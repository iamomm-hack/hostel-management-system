package hotelmanagement.client;

import hotelmanagement.model.Customer;
import hotelmanagement.model.Role;
import hotelmanagement.model.User;

/** Main window for a logged-in customer. */
public class CustomerFrame extends DashboardFrame {
    private static final long serialVersionUID = 1L;

    public CustomerFrame(User user, Customer customer) {
        super("Welcome, " + customer.getFullName(), user);
        addTab("Search & Book Rooms", new RoomBookingPanel(customer.getCustomerId()));
        addTab("My Bookings", new BookingsPanel(Role.CUSTOMER, customer.getCustomerId()));
    }
}
