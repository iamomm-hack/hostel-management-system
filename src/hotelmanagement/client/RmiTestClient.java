package hotelmanagement.client;

import java.time.LocalDate;
import java.util.List;

import hotelmanagement.model.Bill;
import hotelmanagement.model.Booking;
import hotelmanagement.model.BookingStatus;
import hotelmanagement.model.Customer;
import hotelmanagement.model.ExtraService;
import hotelmanagement.model.HotelStatistics;
import hotelmanagement.model.Payment;
import hotelmanagement.model.PaymentMethod;
import hotelmanagement.model.Role;
import hotelmanagement.model.Room;
import hotelmanagement.model.RoomStatus;
import hotelmanagement.model.RoomType;
import hotelmanagement.model.User;
import hotelmanagement.remote.HotelException;

/**
 * Console test of the whole server through RMI (no GUI).
 * Start the server first, then run:
 *     java -cp "out;lib/*" hotelmanagement.client.RmiTestClient
 *
 * NOTE: the test adds a test customer, a few bookings and one payment.
 * Run database/hotel_management.sql again to go back to the clean demo data.
 */
public class RmiTestClient {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) throws Exception {
        String host = args.length > 0 ? args[0] : "localhost";
        try {
            RemoteServices.connect(host);
        } catch (Exception e) {
            System.out.println("Cannot connect to the RMI server on " + host + ". Start the server first.");
            System.exit(1);
        }
        System.out.println("Connected to RMI server on " + host + "\n");

        testAuthentication();
        Customer customer = testRegistration();
        testRooms();
        testBookingRules(customer);
        testStayAndBilling(customer);
        testCustomers();
        testRoomAdministration();
        testConcurrentBooking(customer);
        testStatistics();

        System.out.println("\n==============================");
        System.out.println("PASSED: " + passed + "   FAILED: " + failed);
        System.out.println("==============================");
        System.exit(failed == 0 ? 0 : 1);
    }

    // ---------------------------------------------------------------- tests

    private static void testAuthentication() throws Exception {
        System.out.println("--- Authentication ---");
        User admin = RemoteServices.auth.login("admin", "admin123");
        check("admin login returns ADMIN role", admin.getRole() == Role.ADMIN);

        User reception = RemoteServices.auth.login("reception", "reception123");
        check("receptionist login returns RECEPTIONIST role", reception.getRole() == Role.RECEPTIONIST);

        expectError("wrong password is rejected", () -> RemoteServices.auth.login("admin", "wrong"));
        expectError("empty username is rejected", () -> RemoteServices.auth.login("", "x"));
    }

    private static Customer testRegistration() throws Exception {
        System.out.println("--- Customer registration ---");
        String username = "test" + (System.currentTimeMillis() % 1000000);
        Customer details = new Customer("Test Customer", "9000011111", "test@example.com", "Test Address", "TEST-ID");

        Customer saved = RemoteServices.auth.registerCustomer(username, "secret123", details);
        check("registration returns a customer id", saved.getCustomerId() > 0);

        User user = RemoteServices.auth.login(username, "secret123");
        check("new customer can log in", user.getRole() == Role.CUSTOMER);

        Customer loaded = RemoteServices.customer.getCustomerByUserId(user.getUserId());
        check("login is linked to the customer record", loaded.getCustomerId() == saved.getCustomerId());

        expectError("duplicate username is rejected",
                () -> RemoteServices.auth.registerCustomer(username, "secret123", details));
        expectError("invalid phone is rejected", () -> RemoteServices.auth.registerCustomer(
                username + "x", "secret123", new Customer("A", "123", null, null, null)));
        return saved;
    }

    private static void testRooms() throws Exception {
        System.out.println("--- Rooms ---");
        List<Room> all = RemoteServices.room.getAllRooms();
        check("getAllRooms returns rooms", all.size() >= 12);

        LocalDate in = LocalDate.now().plusDays(40);
        List<Room> doubles = RemoteServices.room.searchAvailableRooms(in, in.plusDays(2), RoomType.DOUBLE, 2);
        boolean onlyDoubles = !doubles.isEmpty();
        for (Room room : doubles) {
            if (room.getRoomType() != RoomType.DOUBLE) {
                onlyDoubles = false;
            }
        }
        check("search by type returns only DOUBLE rooms", onlyDoubles);

        boolean noMaintenance = true;
        for (Room room : RemoteServices.room.searchAvailableRooms(in, in.plusDays(2), null, 1)) {
            if (room.getStatus() == RoomStatus.MAINTENANCE) {
                noMaintenance = false;
            }
        }
        check("maintenance rooms are not offered", noMaintenance);

        expectError("past check-in date is rejected", () -> RemoteServices.room.searchAvailableRooms(
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(1), null, 1));
        expectError("check-out before check-in is rejected",
                () -> RemoteServices.room.searchAvailableRooms(in, in, null, 1));
    }

    private static void testBookingRules(Customer customer) throws Exception {
        System.out.println("--- Booking rules ---");
        LocalDate in = LocalDate.now().plusDays(40);
        LocalDate out = in.plusDays(3);
        Room room = RemoteServices.room.searchAvailableRooms(in, out, RoomType.DOUBLE, 2).get(0);
        int customerId = customer.getCustomerId();

        Booking booking = RemoteServices.booking.createBooking(customerId, room.getRoomId(), in, out, 2);
        check("booking is CONFIRMED", booking.getStatus() == BookingStatus.CONFIRMED);
        check("room status becomes BOOKED",
                RemoteServices.room.getRoomById(room.getRoomId()).getStatus() == RoomStatus.BOOKED);

        expectError("same dates on the same room are rejected",
                () -> RemoteServices.booking.createBooking(customerId, room.getRoomId(), in, out, 2));
        expectError("partly overlapping dates are rejected", () -> RemoteServices.booking.createBooking(
                customerId, room.getRoomId(), in.plusDays(2), out.plusDays(2), 2));
        expectError("too many guests are rejected",
                () -> RemoteServices.booking.createBooking(customerId, room.getRoomId(), out, out.plusDays(1), 9));
        expectError("unknown customer is rejected",
                () -> RemoteServices.booking.createBooking(999999, room.getRoomId(), out, out.plusDays(1), 1));
        expectError("unknown room is rejected",
                () -> RemoteServices.booking.createBooking(customerId, 999999, out, out.plusDays(1), 1));

        boolean stillOffered = false;
        for (Room r : RemoteServices.room.searchAvailableRooms(in, out, null, 1)) {
            if (r.getRoomId() == room.getRoomId()) {
                stillOffered = true;
            }
        }
        check("booked room is no longer offered for those dates", !stillOffered);

        // A stay that starts on the check-out day of the first one does not overlap
        Booking next = RemoteServices.booking.createBooking(customerId, room.getRoomId(), out, out.plusDays(1), 1);
        check("back-to-back booking is allowed", next.getBookingId() > 0);

        expectError("early check-in is rejected", () -> RemoteServices.booking.checkIn(booking.getBookingId()));

        RemoteServices.booking.cancelBooking(next.getBookingId());
        RemoteServices.booking.cancelBooking(booking.getBookingId());
        check("cancelled booking has status CANCELLED", RemoteServices.booking
                .getBookingById(booking.getBookingId()).getStatus() == BookingStatus.CANCELLED);
        check("room is AVAILABLE again after cancelling",
                RemoteServices.room.getRoomById(room.getRoomId()).getStatus() == RoomStatus.AVAILABLE);
        expectError("cancelling twice is rejected",
                () -> RemoteServices.booking.cancelBooking(booking.getBookingId()));
        expectError("a cancelled booking has no bill",
                () -> RemoteServices.payment.getBill(booking.getBookingId()));

        List<Booking> mine = RemoteServices.booking.getBookingsByCustomer(customerId);
        check("customer sees own bookings", mine.size() == 2);
    }

    private static void testStayAndBilling(Customer customer) throws Exception {
        System.out.println("--- Check-in, services, billing, check-out ---");
        LocalDate in = LocalDate.now();
        LocalDate out = in.plusDays(2);
        Room room = RemoteServices.room.searchAvailableRooms(in, out, RoomType.SINGLE, 1).get(0);
        Booking booking = RemoteServices.booking.createBooking(customer.getCustomerId(), room.getRoomId(), in, out, 1);
        int bookingId = booking.getBookingId();

        expectError("services cannot be added before check-in",
                () -> RemoteServices.payment.addServiceUsage(bookingId, 1, 1));
        expectError("check-out before check-in is rejected",
                () -> RemoteServices.booking.checkOut(bookingId, PaymentMethod.CASH));

        RemoteServices.booking.checkIn(bookingId);
        check("booking is CHECKED_IN",
                RemoteServices.booking.getBookingById(bookingId).getStatus() == BookingStatus.CHECKED_IN);
        check("room is OCCUPIED",
                RemoteServices.room.getRoomById(room.getRoomId()).getStatus() == RoomStatus.OCCUPIED);
        expectError("a checked-in booking cannot be cancelled",
                () -> RemoteServices.booking.cancelBooking(bookingId));

        List<ExtraService> services = RemoteServices.payment.getAllServices();
        check("four additional services exist", services.size() == 4);
        ExtraService breakfast = services.get(2);
        ExtraService laundry = services.get(1);
        RemoteServices.payment.addServiceUsage(bookingId, breakfast.getServiceId(), 2);
        RemoteServices.payment.addServiceUsage(bookingId, laundry.getServiceId(), 1);
        expectError("zero quantity is rejected",
                () -> RemoteServices.payment.addServiceUsage(bookingId, breakfast.getServiceId(), 0));

        double roomCharges = 2 * room.getPrice();
        double serviceCharges = 2 * breakfast.getPrice() + laundry.getPrice();
        double gst = (roomCharges + serviceCharges) * 0.12;
        double total = roomCharges + serviceCharges + gst;

        Bill bill = RemoteServices.payment.getBill(bookingId);
        check("room charges = nights x price", same(bill.getRoomCharges(), roomCharges));
        check("service charges are summed", same(bill.getServiceCharges(), serviceCharges));
        check("GST is 12%", same(bill.getGstAmount(), gst));
        check("final amount = room + services + GST", same(bill.getTotalAmount(), total));
        check("bill is not paid before check-out", !bill.isPaid());

        Payment payment = RemoteServices.booking.checkOut(bookingId, PaymentMethod.UPI);
        check("payment saved with the bill total", same(payment.getTotalAmount(), total));
        check("booking is CHECKED_OUT",
                RemoteServices.booking.getBookingById(bookingId).getStatus() == BookingStatus.CHECKED_OUT);
        check("room is AVAILABLE after check-out",
                RemoteServices.room.getRoomById(room.getRoomId()).getStatus() == RoomStatus.AVAILABLE);
        check("final bill is marked paid", RemoteServices.payment.getBill(bookingId).isPaid());
        expectError("checking out twice is rejected",
                () -> RemoteServices.booking.checkOut(bookingId, PaymentMethod.CASH));

        boolean found = false;
        for (Payment p : RemoteServices.payment.getAllPayments()) {
            if (p.getBookingId() == bookingId) {
                found = true;
            }
        }
        check("payment appears in the payment list", found);
    }

    private static void testCustomers() throws Exception {
        System.out.println("--- Customers ---");
        Customer walkIn = RemoteServices.customer.registerWalkInCustomer(
                new Customer("Walkin Tester", "9000022222", null, "Front desk", "WALK-1"));
        check("walk-in customer gets an id", walkIn.getCustomerId() > 0);
        check("walk-in customer has no login", walkIn.getUserId() == 0);

        check("search by name finds the customer",
                !RemoteServices.customer.searchCustomers("Walkin").isEmpty());
        check("search by phone finds the customer",
                !RemoteServices.customer.searchCustomers("9000022222").isEmpty());
        check("getAllCustomers returns customers", RemoteServices.customer.getAllCustomers().size() >= 6);
        expectError("missing customer is reported", () -> RemoteServices.customer.getCustomerById(999999));
    }

    private static void testRoomAdministration() throws Exception {
        System.out.println("--- Room administration ---");
        String number = "T" + (System.currentTimeMillis() % 100000);
        Room room = RemoteServices.room.addRoom(new Room(number, RoomType.DELUXE, 3500, 3, RoomStatus.AVAILABLE));
        check("room is added", room.getRoomId() > 0);
        expectError("duplicate room number is rejected", () -> RemoteServices.room.addRoom(
                new Room(number, RoomType.SINGLE, 1000, 1, RoomStatus.AVAILABLE)));
        expectError("zero price is rejected", () -> RemoteServices.room.addRoom(
                new Room(number + "A", RoomType.SINGLE, 0, 1, RoomStatus.AVAILABLE)));

        room.setPrice(3900);
        room.setCapacity(4);
        RemoteServices.room.updateRoom(room);
        Room updated = RemoteServices.room.getRoomById(room.getRoomId());
        check("room is updated", same(updated.getPrice(), 3900) && updated.getCapacity() == 4);

        RemoteServices.room.updateRoomStatus(room.getRoomId(), RoomStatus.MAINTENANCE);
        check("status set to MAINTENANCE",
                RemoteServices.room.getRoomById(room.getRoomId()).getStatus() == RoomStatus.MAINTENANCE);
        LocalDate in = LocalDate.now().plusDays(60);
        expectError("a maintenance room cannot be booked",
                () -> RemoteServices.booking.createBooking(1, room.getRoomId(), in, in.plusDays(1), 1));
        RemoteServices.room.updateRoomStatus(room.getRoomId(), RoomStatus.AVAILABLE);
        check("status set back to AVAILABLE",
                RemoteServices.room.getRoomById(room.getRoomId()).getStatus() == RoomStatus.AVAILABLE);
        expectError("OCCUPIED cannot be set manually",
                () -> RemoteServices.room.updateRoomStatus(room.getRoomId(), RoomStatus.OCCUPIED));

        RemoteServices.room.deleteRoom(room.getRoomId());
        expectError("deleted room is gone", () -> RemoteServices.room.getRoomById(room.getRoomId()));

        // Room 201 (id 5) has booking history in the demo data
        expectError("a room with bookings cannot be deleted", () -> RemoteServices.room.deleteRoom(5));
        // Room 301 (id 9) has a guest staying in the demo data
        expectError("an occupied room cannot go under maintenance",
                () -> RemoteServices.room.updateRoomStatus(9, RoomStatus.MAINTENANCE));
    }

    /** Two clients try to book the same room for the same dates at the same time. */
    private static void testConcurrentBooking(Customer customer) throws Exception {
        System.out.println("--- Concurrent booking ---");
        LocalDate in = LocalDate.now().plusDays(80);
        LocalDate out = in.plusDays(2);
        Room room = RemoteServices.room.searchAvailableRooms(in, out, RoomType.SUITE, 2).get(0);
        int[] success = new int[1];
        int[] rejected = new int[1];
        Booking[] winner = new Booking[1];

        Runnable attempt = () -> {
            try {
                Booking booking = RemoteServices.booking.createBooking(
                        customer.getCustomerId(), room.getRoomId(), in, out, 2);
                synchronized (success) {
                    success[0]++;
                    winner[0] = booking;
                }
            } catch (HotelException e) {
                synchronized (success) {
                    rejected[0]++;
                }
            } catch (Exception e) {
                System.out.println("  unexpected: " + e);
            }
        };
        Thread[] threads = new Thread[5];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(attempt);
        }
        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
        check("exactly one of 5 simultaneous bookings succeeds (" + success[0] + " ok, "
                + rejected[0] + " rejected)", success[0] == 1 && rejected[0] == 4);
        if (winner[0] != null) {
            RemoteServices.booking.cancelBooking(winner[0].getBookingId());   // leave the room free again
        }
    }

    private static void testStatistics() throws Exception {
        System.out.println("--- Statistics ---");
        HotelStatistics stats = RemoteServices.payment.getStatistics();
        System.out.println("  total rooms       : " + stats.getTotalRooms());
        System.out.println("  available rooms   : " + stats.getAvailableRooms());
        System.out.println("  booked rooms      : " + stats.getBookedRooms());
        System.out.println("  occupied rooms    : " + stats.getOccupiedRooms());
        System.out.println("  maintenance rooms : " + stats.getMaintenanceRooms());
        System.out.println("  total bookings    : " + stats.getTotalBookings());
        System.out.println("  total revenue     : " + stats.getTotalRevenue());
        int sum = stats.getAvailableRooms() + stats.getBookedRooms() + stats.getOccupiedRooms()
                + stats.getMaintenanceRooms();
        check("room status counts add up to total rooms", sum == stats.getTotalRooms());
        check("revenue is positive", stats.getTotalRevenue() > 0);
    }

    // -------------------------------------------------------------- helpers

    /** A remote call that is expected to fail. */
    private interface RemoteCall {
        void run() throws Exception;
    }

    private static void check(String name, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("  PASS  " + name);
        } else {
            failed++;
            System.out.println("  FAIL  " + name);
        }
    }

    /** Passes only if the server refuses the call with a HotelException. */
    private static void expectError(String name, RemoteCall call) {
        try {
            call.run();
            check(name + " (no error was thrown)", false);
        } catch (HotelException e) {
            check(name + "  ->  \"" + e.getMessage().replace("\n", " ") + "\"", true);
        } catch (Exception e) {
            check(name + " (unexpected " + e + ")", false);
        }
    }

    private static boolean same(double a, double b) {
        return Math.abs(a - b) < 0.01;
    }
}
