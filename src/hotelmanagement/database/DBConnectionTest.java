package hotelmanagement.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Small console test for the JDBC connection.
 * Run:  java -cp "out;lib/*" hotelmanagement.database.DBConnectionTest
 */
public class DBConnectionTest {

    public static void main(String[] args) {
        String[] tables = {"users", "customers", "rooms", "bookings", "payments", "services", "service_usage"};

        try (Connection con = DBConnection.getConnection()) {
            System.out.println("Connected to: " + con.getMetaData().getURL());
            System.out.println("MySQL version: " + con.getMetaData().getDatabaseProductVersion());

            for (String table : tables) {
                // table names come from the fixed array above, not from user input
                try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM " + table);
                     ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    System.out.printf("  %-14s %d rows%n", table, rs.getInt(1));
                }
            }
            System.out.println("JDBC connection test PASSED");
        } catch (SQLException e) {
            System.out.println("JDBC connection test FAILED: " + e.getMessage());
            System.exit(1);
        }
    }
}
