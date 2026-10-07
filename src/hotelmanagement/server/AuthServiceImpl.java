package hotelmanagement.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.SQLException;

import hotelmanagement.database.CustomerDAO;
import hotelmanagement.database.DBConnection;
import hotelmanagement.database.UserDAO;
import hotelmanagement.model.Customer;
import hotelmanagement.model.Role;
import hotelmanagement.model.User;
import hotelmanagement.remote.AuthService;
import hotelmanagement.remote.HotelException;

public class AuthServiceImpl extends UnicastRemoteObject implements AuthService {
    private static final long serialVersionUID = 1L;

    private final UserDAO userDAO = new UserDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();

    public AuthServiceImpl() throws RemoteException {
        super();
    }

    @Override
    public User login(String username, String password) throws RemoteException, HotelException {
        if (ServerUtil.isBlank(username) || ServerUtil.isBlank(password)) {
            throw new HotelException("Please enter both username and password.");
        }
        try (Connection con = DBConnection.getConnection()) {
            User user = userDAO.findByLogin(con, username.trim(), ServerUtil.hashPassword(password));
            if (user == null) {
                throw new HotelException("Invalid username or password.");
            }
            System.out.println("[LOGIN] " + user);
            return user;
        } catch (SQLException e) {
            throw ServerUtil.databaseError("login", e);
        }
    }

    @Override
    public Customer registerCustomer(String username, String password, Customer customer)
            throws RemoteException, HotelException {
        if (ServerUtil.isBlank(username) || !username.trim().matches("[A-Za-z0-9_]{3,50}")) {
            throw new HotelException("Username must be 3 to 50 letters, digits or underscores.");
        }
        if (password == null || password.length() < 6) {
            throw new HotelException("Password must be at least 6 characters.");
        }
        ServerUtil.validateCustomer(customer);
        username = username.trim();

        try (Connection con = DBConnection.getConnection()) {
            // The user row and the customer row must be saved together or not at all
            con.setAutoCommit(false);
            try {
                if (userDAO.usernameExists(con, username)) {
                    throw new HotelException("Username '" + username + "' is already taken.");
                }
                int userId = userDAO.insert(con, username, ServerUtil.hashPassword(password), Role.CUSTOMER);
                customer.setUserId(userId);
                int customerId = customerDAO.insert(con, customer);
                con.commit();

                customer.setCustomerId(customerId);
                System.out.println("[REGISTER] customer " + customer + " as user " + username);
                return customer;
            } catch (SQLException | HotelException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw ServerUtil.databaseError("registerCustomer", e);
        }
    }
}
