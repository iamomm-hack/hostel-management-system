package hotelmanagement.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import hotelmanagement.model.Customer;

/** Customer records. */
public interface CustomerService extends Remote {

    /** Registers a walk-in customer (no login account). Returns the saved customer. */
    Customer registerWalkInCustomer(Customer customer) throws RemoteException, HotelException;

    Customer getCustomerById(int customerId) throws RemoteException, HotelException;

    /** The customer record that belongs to a CUSTOMER login. */
    Customer getCustomerByUserId(int userId) throws RemoteException, HotelException;

    List<Customer> getAllCustomers() throws RemoteException, HotelException;

    /** Customers whose name or phone number contains the keyword. */
    List<Customer> searchCustomers(String keyword) throws RemoteException, HotelException;
}
