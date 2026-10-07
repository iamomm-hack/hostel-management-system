package hotelmanagement.client;

import hotelmanagement.model.Role;
import hotelmanagement.model.User;

/** Main window for the receptionist (front desk). */
public class ReceptionistFrame extends DashboardFrame {
    private static final long serialVersionUID = 1L;

    public ReceptionistFrame(User user) {
        super("Reception Desk", user);
        addTab("Bookings", new BookingsPanel(Role.RECEPTIONIST, 0));
        addTab("Customers", new CustomersPanel(true));
        addTab("Book a Room", new RoomBookingPanel(0));
        addTab("Rooms", new RoomsPanel(Role.RECEPTIONIST));
    }
}
