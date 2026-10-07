package hotelmanagement.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;

import hotelmanagement.model.Customer;
import hotelmanagement.model.User;

/** Login and customer registration. */
public interface AuthService extends Remote {

    /** Returns the logged-in user, or throws HotelException if the login is invalid. */
    User login(String username, String password) throws RemoteException, HotelException;

    /**
     * Creates a CUSTOMER login together with the customer's details.
     * Returns the saved customer (with its generated customerId).
     */
    Customer registerCustomer(String username, String password, Customer customer)
            throws RemoteException, HotelException;
}
