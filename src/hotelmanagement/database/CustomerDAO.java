package hotelmanagement.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import hotelmanagement.model.Customer;

/** SQL for the "customers" table. */
public class CustomerDAO {

    private static final String SELECT =
            "SELECT customer_id, user_id, full_name, phone, email, address, id_proof FROM customers";

    /** Inserts a customer and returns the generated customer_id. */
    public int insert(Connection con, Customer customer) throws SQLException {
        String sql = "INSERT INTO customers (user_id, full_name, phone, email, address, id_proof) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (customer.getUserId() > 0) {
                ps.setInt(1, customer.getUserId());
            } else {
                ps.setNull(1, Types.INTEGER);   // walk-in customer: no login account
            }
            ps.setString(2, customer.getFullName());
            ps.setString(3, customer.getPhone());
            ps.setString(4, customer.getEmail());
            ps.setString(5, customer.getAddress());
            ps.setString(6, customer.getIdProof());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    public Customer findById(Connection con, int customerId) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(SELECT + " WHERE customer_id = ?")) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public Customer findByUserId(Connection con, int userId) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(SELECT + " WHERE user_id = ?")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public List<Customer> findAll(Connection con) throws SQLException {
        List<Customer> customers = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(SELECT + " ORDER BY customer_id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                customers.add(mapRow(rs));
            }
        }
        return customers;
    }

    /** Finds customers whose name or phone contains the keyword. */
    public List<Customer> search(Connection con, String keyword) throws SQLException {
        List<Customer> customers = new ArrayList<>();
        String sql = SELECT + " WHERE full_name LIKE ? OR phone LIKE ? ORDER BY full_name";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            String pattern = "%" + keyword + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    customers.add(mapRow(rs));
                }
            }
        }
        return customers;
    }

    private Customer mapRow(ResultSet rs) throws SQLException {
        Customer customer = new Customer();
        customer.setCustomerId(rs.getInt("customer_id"));
        customer.setUserId(rs.getInt("user_id"));   // getInt returns 0 for NULL
        customer.setFullName(rs.getString("full_name"));
        customer.setPhone(rs.getString("phone"));
        customer.setEmail(rs.getString("email"));
        customer.setAddress(rs.getString("address"));
        customer.setIdProof(rs.getString("id_proof"));
        return customer;
    }
}
