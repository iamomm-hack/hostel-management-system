package hotelmanagement.database;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import hotelmanagement.model.Booking;
import hotelmanagement.model.BookingStatus;

/** SQL for the "bookings" table. */
public class BookingDAO {

    // Joins customers and rooms so the client can show the customer name and room number
    private static final String SELECT =
            "SELECT b.booking_id, b.customer_id, b.room_id, b.check_in, b.check_out, b.number_of_guests,"
            + " b.booking_status, b.booking_date, c.full_name, r.room_number"
            + " FROM bookings b"
            + " JOIN customers c ON c.customer_id = b.customer_id"
            + " JOIN rooms r ON r.room_id = b.room_id";

    /**
     * True if the room already has an active booking (CONFIRMED or CHECKED_IN)
     * whose dates overlap the requested dates.
     * Two stays overlap when: existing.check_in < new.check_out AND existing.check_out > new.check_in.
     */
    public boolean hasOverlappingBooking(Connection con, int roomId, LocalDate checkIn, LocalDate checkOut)
            throws SQLException {
        String sql = "SELECT 1 FROM bookings WHERE room_id = ?"
                + " AND booking_status IN ('CONFIRMED', 'CHECKED_IN')"
                + " AND check_in < ? AND check_out > ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            ps.setDate(2, Date.valueOf(checkOut));
            ps.setDate(3, Date.valueOf(checkIn));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Inserts a CONFIRMED booking and returns the generated booking_id. */
    public int insert(Connection con, int customerId, int roomId, LocalDate checkIn, LocalDate checkOut,
                      int guests) throws SQLException {
        String sql = "INSERT INTO bookings (customer_id, room_id, check_in, check_out, number_of_guests,"
                + " booking_status) VALUES (?, ?, ?, ?, ?, 'CONFIRMED')";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, customerId);
            ps.setInt(2, roomId);
            ps.setDate(3, Date.valueOf(checkIn));
            ps.setDate(4, Date.valueOf(checkOut));
            ps.setInt(5, guests);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    public Booking findById(Connection con, int bookingId) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(SELECT + " WHERE b.booking_id = ?")) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public List<Booking> findAll(Connection con) throws SQLException {
        List<Booking> bookings = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(SELECT + " ORDER BY b.booking_id DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                bookings.add(mapRow(rs));
            }
        }
        return bookings;
    }

    public List<Booking> findByCustomer(Connection con, int customerId) throws SQLException {
        List<Booking> bookings = new ArrayList<>();
        String sql = SELECT + " WHERE b.customer_id = ? ORDER BY b.booking_id DESC";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    bookings.add(mapRow(rs));
                }
            }
        }
        return bookings;
    }

    public int updateStatus(Connection con, int bookingId, BookingStatus status) throws SQLException {
        String sql = "UPDATE bookings SET booking_status = ? WHERE booking_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, bookingId);
            return ps.executeUpdate();
        }
    }

    /** Number of bookings of one room that have the given status. */
    public int countByRoomAndStatus(Connection con, int roomId, BookingStatus status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM bookings WHERE room_id = ? AND booking_status = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            ps.setString(2, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    /** Number of bookings (any status) that refer to the room. */
    public int countByRoom(Connection con, int roomId) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM bookings WHERE room_id = ?")) {
            ps.setInt(1, roomId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public int countAll(Connection con) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM bookings");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private Booking mapRow(ResultSet rs) throws SQLException {
        Booking booking = new Booking();
        booking.setBookingId(rs.getInt("booking_id"));
        booking.setCustomerId(rs.getInt("customer_id"));
        booking.setRoomId(rs.getInt("room_id"));
        booking.setCheckIn(rs.getDate("check_in").toLocalDate());
        booking.setCheckOut(rs.getDate("check_out").toLocalDate());
        booking.setNumberOfGuests(rs.getInt("number_of_guests"));
        booking.setStatus(BookingStatus.valueOf(rs.getString("booking_status")));
        booking.setBookingDate(rs.getTimestamp("booking_date").toLocalDateTime());
        booking.setCustomerName(rs.getString("full_name"));
        booking.setRoomNumber(rs.getString("room_number"));
        return booking;
    }
}
