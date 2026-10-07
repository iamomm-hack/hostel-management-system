package hotelmanagement.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import hotelmanagement.database.BookingDAO;
import hotelmanagement.database.DBConnection;
import hotelmanagement.database.PaymentDAO;
import hotelmanagement.database.RoomDAO;
import hotelmanagement.database.ServiceDAO;
import hotelmanagement.model.Bill;
import hotelmanagement.model.Booking;
import hotelmanagement.model.BookingStatus;
import hotelmanagement.model.ExtraService;
import hotelmanagement.model.HotelStatistics;
import hotelmanagement.model.Payment;
import hotelmanagement.model.RoomStatus;
import hotelmanagement.remote.HotelException;
import hotelmanagement.remote.PaymentService;

public class PaymentServiceImpl extends UnicastRemoteObject implements PaymentService {
    private static final long serialVersionUID = 1L;

    private final ServiceDAO serviceDAO = new ServiceDAO();
    private final BookingDAO bookingDAO = new BookingDAO();
    private final RoomDAO roomDAO = new RoomDAO();
    private final PaymentDAO paymentDAO = new PaymentDAO();
    private final BillCalculator billCalculator = new BillCalculator();

    public PaymentServiceImpl() throws RemoteException {
        super();
    }

    @Override
    public List<ExtraService> getAllServices() throws RemoteException, HotelException {
        try (Connection con = DBConnection.getConnection()) {
            return serviceDAO.findAllServices(con);
        } catch (SQLException e) {
            throw ServerUtil.databaseError("getAllServices", e);
        }
    }

    @Override
    public void addServiceUsage(int bookingId, int serviceId, int quantity)
            throws RemoteException, HotelException {
        if (quantity < 1 || quantity > 50) {
            throw new HotelException("Quantity must be between 1 and 50.");
        }
        try (Connection con = DBConnection.getConnection()) {
            Booking booking = bookingDAO.findById(con, bookingId);
            if (booking == null) {
                throw new HotelException("Booking #" + bookingId + " was not found.");
            }
            if (booking.getStatus() != BookingStatus.CHECKED_IN) {
                throw new HotelException("Services can be added only while the guest is checked in.");
            }
            ExtraService service = serviceDAO.findServiceById(con, serviceId);
            if (service == null) {
                throw new HotelException("Service was not found.");
            }
            serviceDAO.insertUsage(con, bookingId, serviceId, quantity);
            System.out.println("[SERVICE] booking #" + bookingId + ": " + service.getServiceName()
                    + " x" + quantity);
        } catch (SQLException e) {
            throw ServerUtil.databaseError("addServiceUsage", e);
        }
    }

    @Override
    public Bill getBill(int bookingId) throws RemoteException, HotelException {
        try (Connection con = DBConnection.getConnection()) {
            return billCalculator.buildBill(con, bookingId);
        } catch (SQLException e) {
            throw ServerUtil.databaseError("getBill", e);
        }
    }

    @Override
    public List<Payment> getAllPayments() throws RemoteException, HotelException {
        try (Connection con = DBConnection.getConnection()) {
            return paymentDAO.findAll(con);
        } catch (SQLException e) {
            throw ServerUtil.databaseError("getAllPayments", e);
        }
    }

    @Override
    public HotelStatistics getStatistics() throws RemoteException, HotelException {
        try (Connection con = DBConnection.getConnection()) {
            HotelStatistics stats = new HotelStatistics();
            stats.setTotalRooms(roomDAO.countAll(con));
            stats.setAvailableRooms(roomDAO.countByStatus(con, RoomStatus.AVAILABLE));
            stats.setBookedRooms(roomDAO.countByStatus(con, RoomStatus.BOOKED));
            stats.setOccupiedRooms(roomDAO.countByStatus(con, RoomStatus.OCCUPIED));
            stats.setMaintenanceRooms(roomDAO.countByStatus(con, RoomStatus.MAINTENANCE));
            stats.setTotalBookings(bookingDAO.countAll(con));
            stats.setTotalRevenue(paymentDAO.totalRevenue(con));
            return stats;
        } catch (SQLException e) {
            throw ServerUtil.databaseError("getStatistics", e);
        }
    }
}
