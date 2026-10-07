package hotelmanagement.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import hotelmanagement.database.CustomerDAO;
import hotelmanagement.database.DBConnection;
import hotelmanagement.model.Customer;
import hotelmanagement.remote.CustomerService;
import hotelmanagement.remote.HotelException;

public class CustomerServiceImpl extends UnicastRemoteObject implements CustomerService {
    private static final long serialVersionUID = 1L;

    private final CustomerDAO customerDAO = new CustomerDAO();

    public CustomerServiceImpl() throws RemoteException {
        super();
    }

    @Override
    public Customer registerWalkInCustomer(Customer customer) throws RemoteException, HotelException {
        ServerUtil.validateCustomer(customer);
        customer.setUserId(0);   // walk-in customers have no login
        try (Connection con = DBConnection.getConnection()) {
            int customerId = customerDAO.insert(con, customer);
            customer.setCustomerId(customerId);
            System.out.println("[WALK-IN] registered customer " + customer);
            return customer;
        } catch (SQLException e) {
            throw ServerUtil.databaseError("registerWalkInCustomer", e);
        }
    }

    @Override
    public Customer getCustomerById(int customerId) throws RemoteException, HotelException {
        try (Connection con = DBConnection.getConnection()) {
            Customer customer = customerDAO.findById(con, customerId);
            if (customer == null) {
                throw new HotelException("Customer #" + customerId + " was not found.");
            }
            return customer;
        } catch (SQLException e) {
            throw ServerUtil.databaseError("getCustomerById", e);
        }
    }

    @Override
    public Customer getCustomerByUserId(int userId) throws RemoteException, HotelException {
        try (Connection con = DBConnection.getConnection()) {
            Customer customer = customerDAO.findByUserId(con, userId);
            if (customer == null) {
                throw new HotelException("No customer record is linked to this login.");
            }
            return customer;
        } catch (SQLException e) {
            throw ServerUtil.databaseError("getCustomerByUserId", e);
        }
    }

    @Override
    public List<Customer> getAllCustomers() throws RemoteException, HotelException {
        try (Connection con = DBConnection.getConnection()) {
            return customerDAO.findAll(con);
        } catch (SQLException e) {
            throw ServerUtil.databaseError("getAllCustomers", e);
        }
    }

    @Override
    public List<Customer> searchCustomers(String keyword) throws RemoteException, HotelException {
        try (Connection con = DBConnection.getConnection()) {
            if (ServerUtil.isBlank(keyword)) {
                return customerDAO.findAll(con);
            }
            return customerDAO.search(con, keyword.trim());
        } catch (SQLException e) {
            throw ServerUtil.databaseError("searchCustomers", e);
        }
    }
}
