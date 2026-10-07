package hotelmanagement.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import hotelmanagement.model.Payment;
import hotelmanagement.model.PaymentMethod;

/** SQL for the "payments" table. */
public class PaymentDAO {

    private static final String SELECT =
            "SELECT p.payment_id, p.booking_id, p.room_charges, p.service_charges, p.gst_amount,"
            + " p.total_amount, p.payment_method, p.payment_date, c.full_name, r.room_number"
            + " FROM payments p"
            + " JOIN bookings b ON b.booking_id = p.booking_id"
            + " JOIN customers c ON c.customer_id = b.customer_id"
            + " JOIN rooms r ON r.room_id = b.room_id";

    /** Inserts a payment and returns the generated payment_id. */
    public int insert(Connection con, Payment payment) throws SQLException {
        String sql = "INSERT INTO payments (booking_id, room_charges, service_charges, gst_amount,"
                + " total_amount, payment_method) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, payment.getBookingId());
            ps.setDouble(2, payment.getRoomCharges());
            ps.setDouble(3, payment.getServiceCharges());
            ps.setDouble(4, payment.getGstAmount());
            ps.setDouble(5, payment.getTotalAmount());
            ps.setString(6, payment.getPaymentMethod().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    public Payment findByBooking(Connection con, int bookingId) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(SELECT + " WHERE p.booking_id = ?")) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public List<Payment> findAll(Connection con) throws SQLException {
        List<Payment> payments = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(SELECT + " ORDER BY p.payment_id DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                payments.add(mapRow(rs));
            }
        }
        return payments;
    }

    /** Sum of all payments received. */
    public double totalRevenue(Connection con) throws SQLException {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM payments";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getDouble(1);
        }
    }

    private Payment mapRow(ResultSet rs) throws SQLException {
        Payment payment = new Payment();
        payment.setPaymentId(rs.getInt("payment_id"));
        payment.setBookingId(rs.getInt("booking_id"));
        payment.setRoomCharges(rs.getDouble("room_charges"));
        payment.setServiceCharges(rs.getDouble("service_charges"));
        payment.setGstAmount(rs.getDouble("gst_amount"));
        payment.setTotalAmount(rs.getDouble("total_amount"));
        payment.setPaymentMethod(PaymentMethod.valueOf(rs.getString("payment_method")));
        payment.setPaymentDate(rs.getTimestamp("payment_date").toLocalDateTime());
        payment.setCustomerName(rs.getString("full_name"));
        payment.setRoomNumber(rs.getString("room_number"));
        return payment;
    }
}
