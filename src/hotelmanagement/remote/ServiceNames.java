package hotelmanagement.remote;

/** Registry port and the names under which the server binds each remote object. */
public final class ServiceNames {

    public static final int RMI_PORT = 1099;

    public static final String AUTH = "AuthService";
    public static final String ROOM = "RoomService";
    public static final String BOOKING = "BookingService";
    public static final String CUSTOMER = "CustomerService";
    public static final String PAYMENT = "PaymentService";

    private ServiceNames() {
        // constants only
    }
}
