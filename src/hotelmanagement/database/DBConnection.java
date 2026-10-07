package hotelmanagement.database;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Single place where the server gets its JDBC connections.
 * The URL, user and password are read from database/db.properties,
 * so no other class contains database credentials.
 */
public class DBConnection {

    private static final String CONFIG_FILE = "database/db.properties";

    private static String url;
    private static String user;
    private static String password;

    private DBConnection() {
        // only static methods
    }

    /** Reads db.properties the first time a connection is requested. */
    private static synchronized void loadConfig() throws SQLException {
        if (url != null) {
            return;
        }
        Properties properties = new Properties();
        try (FileInputStream in = new FileInputStream(CONFIG_FILE)) {
            properties.load(in);
        } catch (IOException e) {
            throw new SQLException("Cannot read " + CONFIG_FILE
                    + ". Start the server from the project folder.", e);
        }
        user = properties.getProperty("db.user");
        password = properties.getProperty("db.password");
        url = properties.getProperty("db.url");
        if (url == null || user == null || password == null) {
            url = null;
            throw new SQLException(CONFIG_FILE + " must contain db.url, db.user and db.password.");
        }
    }

    /**
     * Opens a new connection to the hotel_management database.
     * The caller must close it (use try-with-resources).
     */
    public static Connection getConnection() throws SQLException {
        loadConfig();
        return DriverManager.getConnection(url, user, password);
    }
}
