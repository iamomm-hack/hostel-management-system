package hotelmanagement.server;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import hotelmanagement.model.Customer;
import hotelmanagement.remote.HotelException;

/** Small helper methods shared by the service implementations. */
public class ServerUtil {

    private ServerUtil() {
        // only static methods
    }

    /**
     * Logs the real SQL error on the server console and returns a
     * user-friendly exception for the client. The client never sees SQL details.
     */
    public static HotelException databaseError(String operation, SQLException e) {
        System.err.println("[DB ERROR] " + operation + ": " + e.getMessage());

        // SQL states starting with "08" mean the connection itself failed
        String sqlState = e.getSQLState();
        if (sqlState != null && sqlState.startsWith("08")) {
            return new HotelException("The server cannot connect to the database. Please try again later.");
        }
        return new HotelException("A database error occurred. Please try again.");
    }

    /** SHA-256 hash of the password as 64 hex characters (same as MySQL's SHA2(x, 256)). */
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is available in every JDK
            throw new IllegalStateException(e);
        }
    }

    public static boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }

    /** Checks the customer details sent by a client. */
    public static void validateCustomer(Customer customer) throws HotelException {
        if (customer == null) {
            throw new HotelException("Customer details are missing.");
        }
        if (isBlank(customer.getFullName())) {
            throw new HotelException("Full name is required.");
        }
        if (customer.getPhone() == null || !customer.getPhone().matches("\\d{10}")) {
            throw new HotelException("Phone number must be exactly 10 digits.");
        }
        if (!isBlank(customer.getEmail()) && !customer.getEmail().matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            throw new HotelException("Email address is not valid.");
        }
    }

    /** Rounds an amount to 2 decimal places. */
    public static double round2(double amount) {
        return Math.round(amount * 100.0) / 100.0;
    }
}
