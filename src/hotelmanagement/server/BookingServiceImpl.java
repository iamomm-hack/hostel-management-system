package hotelmanagement.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import hotelmanagement.database.BookingDAO;
import hotelmanagement.database.CustomerDAO;
import hotelmanagement.database.DBConnection;
import hotelmanagement.database.PaymentDAO;
import hotelmanagement.database.RoomDAO;
import hotelmanagement.model.Bill;
import hotelmanagement.model.Booking;
import hotelmanagement.model.BookingStatus;
import hotelmanagement.model.Payment;
import hotelmanagement.model.PaymentMethod;
import hotelmanagement.model.Room;
import hotelmanagement.model.RoomStatus;
import hotelmanagement.remote.BookingService;
import hotelmanagement.remote.HotelException;

public class BookingServiceImpl extends UnicastRemoteObject implements BookingService {
    private static final long serialVersionUID = 1L;

    private final BookingDAO bookingDAO = new BookingDAO();
    private final RoomDAO roomDAO = new RoomDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final PaymentDAO paymentDAO = new PaymentDAO();
    private final BillCalculator billCalculator = new BillCalculator();

    public BookingServiceImpl() throws RemoteException {
        super();
    }

    /**
     * RMI serves every client call on its own thread, so two clients can ask for
     * the same room at the same moment. Double booking is prevented in two ways:
     * the method is synchronized, and the room row is locked (SELECT ... FOR UPDATE)
     * inside a transaction while the overlap check and the insert are done.
     */
    @Override
    public synchronized Booking createBooking(int customerId, int roomId, LocalDate checkIn,
                                              LocalDate checkOut, int guests)
            throws RemoteException, HotelException {
        RoomServiceImpl.validateStayDates(checkIn, checkOut);
        if (guests < 1) {
            throw new HotelException("Number of guests must be at least 1.");
        }

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                if (customerDAO.findById(con, customerId) == null) {
                    throw new HotelException("Customer #" + customerId + " was not found.");
                }
                Room room = roomDAO.findByIdForUpdate(con, roomId);
                if (room == null) {
                    throw new HotelException("Room was not found.");
                }
                if (room.getStatus() == RoomStatus.MAINTENANCE) {
                    throw new HotelException("Room " + room.getRoomNumber() + " is under maintenance.");
                }
                if (guests > room.getCapacity()) {
                    throw new HotelException("Room " + room.getRoomNumber() + " can hold only "
                            + room.getCapacity() + " guest(s).");
                }
                if (bookingDAO.hasOverlappingBooking(con, roomId, checkIn, checkOut)) {
                    throw new HotelException("Room " + room.getRoomNumber()
                            + " is already booked for the selected dates.");
                }

                int bookingId = bookingDAO.insert(con, customerId, roomId, checkIn, checkOut, guests);
                roomDAO.refreshStatusFromBookings(con, roomId);
                Booking booking = bookingDAO.findById(con, bookingId);
                con.commit();

                System.out.println("[BOOKING] created " + booking);
                return booking;
            } catch (SQLException | HotelException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw ServerUtil.databaseError("createBooking", e);
        }
    }

    @Override
    public void cancelBooking(int bookingId) throws RemoteException, HotelException {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                Booking booking = findBooking(con, bookingId);
                if (booking.getStatus() != BookingStatus.CONFIRMED) {
                    throw new HotelException("Only CONFIRMED bookings can be cancelled. This booking is "
                            + booking.getStatus() + ".");
                }
                bookingDAO.updateStatus(con, bookingId, BookingStatus.CANCELLED);
                roomDAO.refreshStatusFromBookings(con, booking.getRoomId());
                con.commit();
                System.out.println("[BOOKING] cancelled #" + bookingId);
            } catch (SQLException | HotelException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw ServerUtil.databaseError("cancelBooking", e);
        }
    }

    @Override
    public void checkIn(int bookingId) throws RemoteException, HotelException {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                Booking booking = findBooking(con, bookingId);
                if (booking.getStatus() != BookingStatus.CONFIRMED) {
                    throw new HotelException("Only CONFIRMED bookings can be checked in. This booking is "
                            + booking.getStatus() + ".");
                }
                LocalDate today = LocalDate.now();
                if (today.isBefore(booking.getCheckIn())) {
                    throw new HotelException("Check-in is allowed only from " + booking.getCheckIn() + ".");
                }
                if (!today.isBefore(booking.getCheckOut())) {
                    throw new HotelException("The stay dates of this booking are over. Please cancel it.");
                }
                bookingDAO.updateStatus(con, bookingId, BookingStatus.CHECKED_IN);
                roomDAO.refreshStatusFromBookings(con, booking.getRoomId());
                con.commit();
                System.out.println("[CHECK-IN] booking #" + bookingId + ", room " + booking.getRoomNumber());
            } catch (SQLException | HotelException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw ServerUtil.databaseError("checkIn", e);
        }
    }

    @Override
    public Payment checkOut(int bookingId, PaymentMethod paymentMethod) throws RemoteException, HotelException {
        if (paymentMethod == null) {
            throw new HotelException("Please select a payment method.");
        }
        try (Connection con = DBConnection.getConnection()) {
            // Payment, booking status and room status must all change together
            con.setAutoCommit(false);
            try {
                Booking booking = findBooking(con, bookingId);
                if (booking.getStatus() != BookingStatus.CHECKED_IN) {
                    throw new HotelException("Only CHECKED_IN bookings can be checked out. This booking is "
                            + booking.getStatus() + ".");
                }
                Bill bill = billCalculator.buildBill(con, bookingId);

                Payment payment = new Payment();
                payment.setBookingId(bookingId);
                payment.setRoomCharges(bill.getRoomCharges());
                payment.setServiceCharges(bill.getServiceCharges());
                payment.setGstAmount(bill.getGstAmount());
                payment.setTotalAmount(bill.getTotalAmount());
                payment.setPaymentMethod(paymentMethod);
                paymentDAO.insert(con, payment);

                bookingDAO.updateStatus(con, bookingId, BookingStatus.CHECKED_OUT);
                roomDAO.refreshStatusFromBookings(con, booking.getRoomId());
                Payment saved = paymentDAO.findByBooking(con, bookingId);
                con.commit();

                System.out.println("[CHECK-OUT] booking #" + bookingId + ", paid Rs. "
                        + saved.getTotalAmount() + " by " + paymentMethod);
                return saved;
            } catch (SQLException | HotelException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw ServerUtil.databaseError("checkOut", e);
        }
    }

    @Override
    public Booking getBookingById(int bookingId) throws RemoteException, HotelException {
        try (Connection con = DBConnection.getConnection()) {
            return findBooking(con, bookingId);
        } catch (SQLException e) {
            throw ServerUtil.databaseError("getBookingById", e);
        }
    }

    @Override
    public List<Booking> getAllBookings() throws RemoteException, HotelException {
        try (Connection con = DBConnection.getConnection()) {
            return bookingDAO.findAll(con);
        } catch (SQLException e) {
            throw ServerUtil.databaseError("getAllBookings", e);
        }
    }

    @Override
    public List<Booking> getBookingsByCustomer(int customerId) throws RemoteException, HotelException {
        try (Connection con = DBConnection.getConnection()) {
            return bookingDAO.findByCustomer(con, customerId);
        } catch (SQLException e) {
            throw ServerUtil.databaseError("getBookingsByCustomer", e);
        }
    }

    private Booking findBooking(Connection con, int bookingId) throws SQLException, HotelException {
        Booking booking = bookingDAO.findById(con, bookingId);
        if (booking == null) {
            throw new HotelException("Booking #" + bookingId + " was not found.");
        }
        return booking;
    }
}
