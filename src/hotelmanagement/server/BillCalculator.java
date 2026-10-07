package hotelmanagement.server;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.temporal.ChronoUnit;
import java.util.List;

import hotelmanagement.database.BookingDAO;
import hotelmanagement.database.PaymentDAO;
import hotelmanagement.database.RoomDAO;
import hotelmanagement.database.ServiceDAO;
import hotelmanagement.model.Bill;
import hotelmanagement.model.Booking;
import hotelmanagement.model.BookingStatus;
import hotelmanagement.model.Payment;
import hotelmanagement.model.Room;
import hotelmanagement.model.ServiceUsage;
import hotelmanagement.remote.HotelException;

/**
 * Calculates the bill of a booking on the server:
 *
 *   Room Charges    = number of nights x room price
 *   Service Charges = sum of (service price x quantity)
 *   GST             = 12% of (Room Charges + Service Charges)
 *   Final Amount    = Room Charges + Service Charges + GST
 */
public class BillCalculator {

    public static final double GST_PERCENT = 12.0;

    private final BookingDAO bookingDAO = new BookingDAO();
    private final RoomDAO roomDAO = new RoomDAO();
    private final ServiceDAO serviceDAO = new ServiceDAO();
    private final PaymentDAO paymentDAO = new PaymentDAO();

    public Bill buildBill(Connection con, int bookingId) throws SQLException, HotelException {
        Booking booking = bookingDAO.findById(con, bookingId);
        if (booking == null) {
            throw new HotelException("Booking #" + bookingId + " was not found.");
        }
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new HotelException("This booking was cancelled, so it has no bill.");
        }
        Room room = roomDAO.findById(con, booking.getRoomId());
        List<ServiceUsage> services = serviceDAO.findUsageByBooking(con, bookingId);

        Bill bill = new Bill();
        bill.setBookingId(bookingId);
        bill.setCustomerName(booking.getCustomerName());
        bill.setRoomNumber(room.getRoomNumber());
        bill.setRoomType(room.getRoomType());
        bill.setCheckIn(booking.getCheckIn());
        bill.setCheckOut(booking.getCheckOut());
        bill.setBookingStatus(booking.getStatus());
        bill.setNights(ChronoUnit.DAYS.between(booking.getCheckIn(), booking.getCheckOut()));
        bill.setPricePerNight(room.getPrice());
        bill.setServices(services);
        bill.setGstPercent(GST_PERCENT);

        Payment payment = paymentDAO.findByBooking(con, bookingId);
        if (payment != null) {
            // Already paid: show exactly the amounts that were charged at check-out
            bill.setRoomCharges(payment.getRoomCharges());
            bill.setServiceCharges(payment.getServiceCharges());
            bill.setGstAmount(payment.getGstAmount());
            bill.setTotalAmount(payment.getTotalAmount());
            bill.setPaid(true);
            bill.setPaymentMethod(payment.getPaymentMethod());
            return bill;
        }

        double roomCharges = bill.getNights() * room.getPrice();
        double serviceCharges = 0;
        for (ServiceUsage usage : services) {
            serviceCharges += usage.getAmount();
        }
        double gstAmount = (roomCharges + serviceCharges) * GST_PERCENT / 100.0;

        bill.setRoomCharges(ServerUtil.round2(roomCharges));
        bill.setServiceCharges(ServerUtil.round2(serviceCharges));
        bill.setGstAmount(ServerUtil.round2(gstAmount));
        bill.setTotalAmount(ServerUtil.round2(roomCharges + serviceCharges + gstAmount));
        bill.setPaid(false);
        return bill;
    }
}
