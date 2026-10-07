package hotelmanagement.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import hotelmanagement.database.BookingDAO;
import hotelmanagement.database.DBConnection;
import hotelmanagement.database.RoomDAO;
import hotelmanagement.model.BookingStatus;
import hotelmanagement.model.Room;
import hotelmanagement.model.RoomStatus;
import hotelmanagement.model.RoomType;
import hotelmanagement.remote.HotelException;
import hotelmanagement.remote.RoomService;

public class RoomServiceImpl extends UnicastRemoteObject implements RoomService {
    private static final long serialVersionUID = 1L;

    private final RoomDAO roomDAO = new RoomDAO();
    private final BookingDAO bookingDAO = new BookingDAO();

    public RoomServiceImpl() throws RemoteException {
        super();
    }

    @Override
    public List<Room> getAllRooms() throws RemoteException, HotelException {
        try (Connection con = DBConnection.getConnection()) {
            return roomDAO.findAll(con);
        } catch (SQLException e) {
            throw ServerUtil.databaseError("getAllRooms", e);
        }
    }

    @Override
    public Room getRoomById(int roomId) throws RemoteException, HotelException {
        try (Connection con = DBConnection.getConnection()) {
            Room room = roomDAO.findById(con, roomId);
            if (room == null) {
                throw new HotelException("Room was not found.");
            }
            return room;
        } catch (SQLException e) {
            throw ServerUtil.databaseError("getRoomById", e);
        }
    }

    @Override
    public List<Room> searchAvailableRooms(LocalDate checkIn, LocalDate checkOut, RoomType roomType, int guests)
            throws RemoteException, HotelException {
        validateStayDates(checkIn, checkOut);
        if (guests < 1) {
            throw new HotelException("Number of guests must be at least 1.");
        }
        try (Connection con = DBConnection.getConnection()) {
            return roomDAO.findAvailable(con, checkIn, checkOut, roomType, guests);
        } catch (SQLException e) {
            throw ServerUtil.databaseError("searchAvailableRooms", e);
        }
    }

    @Override
    public Room addRoom(Room room) throws RemoteException, HotelException {
        validateRoom(room);
        try (Connection con = DBConnection.getConnection()) {
            if (roomDAO.roomNumberExists(con, room.getRoomNumber(), 0)) {
                throw new HotelException("Room number " + room.getRoomNumber() + " already exists.");
            }
            room.setStatus(RoomStatus.AVAILABLE);
            room.setRoomId(roomDAO.insert(con, room));
            System.out.println("[ROOM] added " + room);
            return room;
        } catch (SQLException e) {
            throw ServerUtil.databaseError("addRoom", e);
        }
    }

    @Override
    public void updateRoom(Room room) throws RemoteException, HotelException {
        validateRoom(room);
        try (Connection con = DBConnection.getConnection()) {
            if (roomDAO.roomNumberExists(con, room.getRoomNumber(), room.getRoomId())) {
                throw new HotelException("Room number " + room.getRoomNumber() + " already exists.");
            }
            if (roomDAO.update(con, room) == 0) {
                throw new HotelException("Room was not found.");
            }
            System.out.println("[ROOM] updated " + room);
        } catch (SQLException e) {
            throw ServerUtil.databaseError("updateRoom", e);
        }
    }

    @Override
    public void deleteRoom(int roomId) throws RemoteException, HotelException {
        try (Connection con = DBConnection.getConnection()) {
            // Bookings refer to the room (foreign key), so a room with history cannot be deleted
            if (bookingDAO.countByRoom(con, roomId) > 0) {
                throw new HotelException("This room has booking history and cannot be deleted.\n"
                        + "Set its status to MAINTENANCE instead.");
            }
            if (roomDAO.delete(con, roomId) == 0) {
                throw new HotelException("Room was not found.");
            }
            System.out.println("[ROOM] deleted room id " + roomId);
        } catch (SQLException e) {
            throw ServerUtil.databaseError("deleteRoom", e);
        }
    }

    @Override
    public void updateRoomStatus(int roomId, RoomStatus status) throws RemoteException, HotelException {
        if (status != RoomStatus.AVAILABLE && status != RoomStatus.MAINTENANCE) {
            throw new HotelException("Only AVAILABLE or MAINTENANCE can be set manually.\n"
                    + "BOOKED and OCCUPIED are set automatically by booking and check-in.");
        }
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                Room room = roomDAO.findByIdForUpdate(con, roomId);
                if (room == null) {
                    throw new HotelException("Room was not found.");
                }
                if (status == RoomStatus.MAINTENANCE) {
                    int active = bookingDAO.countByRoomAndStatus(con, roomId, BookingStatus.CONFIRMED)
                            + bookingDAO.countByRoomAndStatus(con, roomId, BookingStatus.CHECKED_IN);
                    if (active > 0) {
                        throw new HotelException("Room " + room.getRoomNumber()
                                + " has active bookings and cannot be put under maintenance.");
                    }
                    roomDAO.updateStatus(con, roomId, RoomStatus.MAINTENANCE);
                } else {
                    // Back in service: the real status depends on the room's bookings
                    roomDAO.refreshStatusFromBookings(con, roomId);
                }
                con.commit();
                System.out.println("[ROOM] status of room " + room.getRoomNumber() + " set to " + status);
            } catch (SQLException | HotelException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw ServerUtil.databaseError("updateRoomStatus", e);
        }
    }

    /** Used by room search and by booking. */
    static void validateStayDates(LocalDate checkIn, LocalDate checkOut) throws HotelException {
        if (checkIn == null || checkOut == null) {
            throw new HotelException("Check-in and check-out dates are required.");
        }
        if (checkIn.isBefore(LocalDate.now())) {
            throw new HotelException("Check-in date cannot be in the past.");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new HotelException("Check-out date must be after the check-in date.");
        }
    }

    private void validateRoom(Room room) throws HotelException {
        if (room == null) {
            throw new HotelException("Room details are missing.");
        }
        if (ServerUtil.isBlank(room.getRoomNumber()) || room.getRoomNumber().trim().length() > 10) {
            throw new HotelException("Room number is required (maximum 10 characters).");
        }
        room.setRoomNumber(room.getRoomNumber().trim());
        if (room.getRoomType() == null) {
            throw new HotelException("Room type is required.");
        }
        if (room.getPrice() <= 0) {
            throw new HotelException("Price must be greater than 0.");
        }
        if (room.getCapacity() < 1 || room.getCapacity() > 10) {
            throw new HotelException("Capacity must be between 1 and 10.");
        }
    }
}
