package hotelmanagement.client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import hotelmanagement.remote.AuthService;
import hotelmanagement.remote.BookingService;
import hotelmanagement.remote.CustomerService;
import hotelmanagement.remote.PaymentService;
import hotelmanagement.remote.RoomService;
import hotelmanagement.remote.ServiceNames;

/**
 * Looks up the five remote objects in the RMI registry once and keeps the stubs.
 * Every screen of the client uses these stubs; the client has no JDBC code at all.
 */
public class RemoteServices {

    public static AuthService auth;
    public static RoomService room;
    public static BookingService booking;
    public static CustomerService customer;
    public static PaymentService payment;

    private RemoteServices() {
        // only static members
    }

    /** Connects to the RMI registry on the given host and looks up all services. */
    public static void connect(String host) throws RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry(host, ServiceNames.RMI_PORT);
        auth = (AuthService) registry.lookup(ServiceNames.AUTH);
        room = (RoomService) registry.lookup(ServiceNames.ROOM);
        booking = (BookingService) registry.lookup(ServiceNames.BOOKING);
        customer = (CustomerService) registry.lookup(ServiceNames.CUSTOMER);
        payment = (PaymentService) registry.lookup(ServiceNames.PAYMENT);
    }
}
