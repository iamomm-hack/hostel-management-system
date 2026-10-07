package hotelmanagement.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.util.List;

import hotelmanagement.model.Room;
import hotelmanagement.model.RoomStatus;
import hotelmanagement.model.RoomType;

/** Viewing, searching and managing rooms. */
public interface RoomService extends Remote {

    List<Room> getAllRooms() throws RemoteException, HotelException;

    Room getRoomById(int roomId) throws RemoteException, HotelException;

    /**
     * Rooms that are free for the whole stay and can hold the given number of guests.
     * roomType may be null to search all room types.
     */
    List<Room> searchAvailableRooms(LocalDate checkIn, LocalDate checkOut, RoomType roomType, int guests)
            throws RemoteException, HotelException;

    /** Adds a room (ADMIN). Returns the saved room with its generated roomId. */
    Room addRoom(Room room) throws RemoteException, HotelException;

    /** Updates room number, type, price and capacity (ADMIN). */
    void updateRoom(Room room) throws RemoteException, HotelException;

    /** Deletes a room that has never been booked (ADMIN). */
    void deleteRoom(int roomId) throws RemoteException, HotelException;

    /**
     * Manual status change: MAINTENANCE takes a room out of service,
     * AVAILABLE puts it back. BOOKED and OCCUPIED are set automatically.
     */
    void updateRoomStatus(int roomId, RoomStatus status) throws RemoteException, HotelException;
}
