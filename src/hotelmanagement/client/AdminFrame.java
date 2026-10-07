package hotelmanagement.client;

import hotelmanagement.model.Role;
import hotelmanagement.model.User;

/** Main window for the administrator. */
public class AdminFrame extends DashboardFrame {
    private static final long serialVersionUID = 1L;

    public AdminFrame(User user) {
        super("Admin Dashboard", user);
        addTab("Statistics", new StatisticsPanel());
        addTab("Rooms", new RoomsPanel(Role.ADMIN));
        addTab("Customers", new CustomersPanel(false));
        addTab("Bookings", new BookingsPanel(Role.ADMIN, 0));
        addTab("Payments", new PaymentsPanel());
    }
}
