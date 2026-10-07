package hotelmanagement.server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.sql.Connection;
import java.sql.SQLException;

import hotelmanagement.database.DBConnection;
import hotelmanagement.remote.ServiceNames;

/**
 * Starts the RMI server.
 * Run from the project folder:  java -cp "out;lib/*" hotelmanagement.server.HotelServer
 */
public class HotelServer {

    // Kept in a static field so the registry is not garbage collected
    private static Registry registry;

    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println(" Hotel Management System - RMI Server");
        System.out.println("==============================================");

        // Step 0: make sure the database can be reached before accepting clients
        try (Connection con = DBConnection.getConnection()) {
            System.out.println("[OK] Connected to MySQL: " + con.getMetaData().getURL());
        } catch (SQLException e) {
            System.out.println("[FAILED] Cannot connect to MySQL: " + e.getMessage());
            System.out.println("Check that MySQL is running and database/db.properties is correct.");
            System.exit(1);
        }

        try {
            // Step 1: start the RMI registry inside this JVM
            registry = LocateRegistry.createRegistry(ServiceNames.RMI_PORT);
            System.out.println("[OK] RMI registry started on port " + ServiceNames.RMI_PORT);

            // Step 2: create the remote objects and Step 3: bind them by name
            registry.rebind(ServiceNames.AUTH, new AuthServiceImpl());
            System.out.println("[OK] Bound " + ServiceNames.AUTH);

            registry.rebind(ServiceNames.ROOM, new RoomServiceImpl());
            System.out.println("[OK] Bound " + ServiceNames.ROOM);

            registry.rebind(ServiceNames.BOOKING, new BookingServiceImpl());
            System.out.println("[OK] Bound " + ServiceNames.BOOKING);

            registry.rebind(ServiceNames.CUSTOMER, new CustomerServiceImpl());
            System.out.println("[OK] Bound " + ServiceNames.CUSTOMER);

            registry.rebind(ServiceNames.PAYMENT, new PaymentServiceImpl());
            System.out.println("[OK] Bound " + ServiceNames.PAYMENT);

            System.out.println("----------------------------------------------");
            System.out.println("Server is ready. Waiting for clients...");
            System.out.println("Press Ctrl+C to stop the server.");
        } catch (RemoteException e) {
            System.out.println("[FAILED] Could not start the RMI server: " + e.getMessage());
            System.out.println("Is another server already running on port " + ServiceNames.RMI_PORT + "?");
            System.exit(1);
        }
    }
}
