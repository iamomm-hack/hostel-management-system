package hotelmanagement.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import hotelmanagement.model.Bill;
import hotelmanagement.model.ExtraService;
import hotelmanagement.model.HotelStatistics;
import hotelmanagement.model.Payment;

/** Additional services, bills, payments and hotel statistics. */
public interface PaymentService extends Remote {

    /** The additional services a guest can use (Room Service, Laundry, ...). */
    List<ExtraService> getAllServices() throws RemoteException, HotelException;

    /** Adds a service to the bill of a guest who is currently checked in. */
    void addServiceUsage(int bookingId, int serviceId, int quantity) throws RemoteException, HotelException;

    /**
     * The bill of a booking: Room Charges + Service Charges + GST = Final Amount.
     * Before check-out it is the bill so far; after check-out it is the paid final bill.
     */
    Bill getBill(int bookingId) throws RemoteException, HotelException;

    List<Payment> getAllPayments() throws RemoteException, HotelException;

    /** Room counts, number of bookings and total revenue (ADMIN). */
    HotelStatistics getStatistics() throws RemoteException, HotelException;
}
