package hotelmanagement.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.util.List;

import hotelmanagement.model.Booking;
import hotelmanagement.model.Payment;
import hotelmanagement.model.PaymentMethod;

/** Bookings, check-in and check-out. */
public interface BookingService extends Remote {

    /**
     * Books a room after the server has checked the dates, the capacity
     * and that no other booking overlaps. Returns the CONFIRMED booking.
     */
    Booking createBooking(int customerId, int roomId, LocalDate checkIn, LocalDate checkOut, int guests)
            throws RemoteException, HotelException;

    /** Cancels a CONFIRMED booking. */
    void cancelBooking(int bookingId) throws RemoteException, HotelException;

    /** CONFIRMED -> CHECKED_IN, room becomes OCCUPIED. */
    void checkIn(int bookingId) throws RemoteException, HotelException;

    /**
     * CHECKED_IN -> CHECKED_OUT. Calculates the final bill, saves the payment
     * and frees the room, all in one transaction. Returns the saved payment.
     */
    Payment checkOut(int bookingId, PaymentMethod paymentMethod) throws RemoteException, HotelException;

    Booking getBookingById(int bookingId) throws RemoteException, HotelException;

    List<Booking> getAllBookings() throws RemoteException, HotelException;

    List<Booking> getBookingsByCustomer(int customerId) throws RemoteException, HotelException;
}
