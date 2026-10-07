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

import hotelmanagement.model.Room;
import hotelmanagement.model.RoomStatus;
import hotelmanagement.model.RoomType;

/** SQL for the "rooms" table. */
public class RoomDAO {

    private static final String SELECT =
            "SELECT room_id, room_number, room_type, price, capacity, status FROM rooms";

    public List<Room> findAll(Connection con) throws SQLException {
        List<Room> rooms = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(SELECT + " ORDER BY room_number");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rooms.add(mapRow(rs));
            }
        }
        return rooms;
    }

    public Room findById(Connection con, int roomId) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(SELECT + " WHERE room_id = ?")) {
            ps.setInt(1, roomId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    /**
     * Same as findById but locks the row until the transaction ends (SELECT ... FOR UPDATE).
     * Used while booking, so two clients cannot book the same room at the same moment.
     */
    public Room findByIdForUpdate(Connection con, int roomId) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(SELECT + " WHERE room_id = ? FOR UPDATE")) {
            ps.setInt(1, roomId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    /**
     * Rooms that can be booked for the given dates: not under maintenance,
     * big enough for the guests, and with no active booking that overlaps the dates.
     * roomType may be null to search all types.
     */
    public List<Room> findAvailable(Connection con, LocalDate checkIn, LocalDate checkOut,
                                    RoomType roomType, int guests) throws SQLException {
        String sql = SELECT + " r WHERE r.status <> 'MAINTENANCE' AND r.capacity >= ?"
                + " AND NOT EXISTS (SELECT 1 FROM bookings b WHERE b.room_id = r.room_id"
                + " AND b.booking_status IN ('CONFIRMED', 'CHECKED_IN')"
                + " AND b.check_in < ? AND b.check_out > ?)";
        if (roomType != null) {
            sql += " AND r.room_type = ?";
        }
        sql += " ORDER BY r.price, r.room_number";

        List<Room> rooms = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, guests);
            ps.setDate(2, Date.valueOf(checkOut));
            ps.setDate(3, Date.valueOf(checkIn));
            if (roomType != null) {
                ps.setString(4, roomType.name());
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rooms.add(mapRow(rs));
                }
            }
        }
        return rooms;
    }

    /** True if another room (not excludeRoomId) already uses this room number. */
    public boolean roomNumberExists(Connection con, String roomNumber, int excludeRoomId) throws SQLException {
        String sql = "SELECT 1 FROM rooms WHERE room_number = ? AND room_id <> ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, roomNumber);
            ps.setInt(2, excludeRoomId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Inserts a room and returns the generated room_id. */
    public int insert(Connection con, Room room) throws SQLException {
        String sql = "INSERT INTO rooms (room_number, room_type, price, capacity, status) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, room.getRoomNumber());
            ps.setString(2, room.getRoomType().name());
            ps.setDouble(3, room.getPrice());
            ps.setInt(4, room.getCapacity());
            ps.setString(5, room.getStatus().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    /** Updates number, type, price and capacity. The status is changed separately. */
    public int update(Connection con, Room room) throws SQLException {
        String sql = "UPDATE rooms SET room_number = ?, room_type = ?, price = ?, capacity = ? WHERE room_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, room.getRoomNumber());
            ps.setString(2, room.getRoomType().name());
            ps.setDouble(3, room.getPrice());
            ps.setInt(4, room.getCapacity());
            ps.setInt(5, room.getRoomId());
            return ps.executeUpdate();
        }
    }

    public int updateStatus(Connection con, int roomId, RoomStatus status) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("UPDATE rooms SET status = ? WHERE room_id = ?")) {
            ps.setString(1, status.name());
            ps.setInt(2, roomId);
            return ps.executeUpdate();
        }
    }

    /**
     * Sets the room status from its bookings:
     * OCCUPIED if a guest is checked in, BOOKED if a confirmed booking exists, otherwise AVAILABLE.
     * Called after a booking is created, cancelled, checked in or checked out.
     */
    public void refreshStatusFromBookings(Connection con, int roomId) throws SQLException {
        String sql = "UPDATE rooms SET status = CASE"
                + " WHEN EXISTS (SELECT 1 FROM bookings WHERE room_id = ? AND booking_status = 'CHECKED_IN')"
                + " THEN 'OCCUPIED'"
                + " WHEN EXISTS (SELECT 1 FROM bookings WHERE room_id = ? AND booking_status = 'CONFIRMED')"
                + " THEN 'BOOKED'"
                + " ELSE 'AVAILABLE' END"
                + " WHERE room_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            ps.setInt(2, roomId);
            ps.setInt(3, roomId);
            ps.executeUpdate();
        }
    }

    public int delete(Connection con, int roomId) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("DELETE FROM rooms WHERE room_id = ?")) {
            ps.setInt(1, roomId);
            return ps.executeUpdate();
        }
    }

    public int countAll(Connection con) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM rooms");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    public int countByStatus(Connection con, RoomStatus status) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM rooms WHERE status = ?")) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private Room mapRow(ResultSet rs) throws SQLException {
        Room room = new Room();
        room.setRoomId(rs.getInt("room_id"));
        room.setRoomNumber(rs.getString("room_number"));
        room.setRoomType(RoomType.valueOf(rs.getString("room_type")));
        room.setPrice(rs.getDouble("price"));
        room.setCapacity(rs.getInt("capacity"));
        room.setStatus(RoomStatus.valueOf(rs.getString("status")));
        return room;
    }
}
