package hotelmanagement.remote;

/**
 * Thrown by the server when a request cannot be completed for a business reason
 * (invalid login, room not available, invalid dates, ...).
 * The message is written for the end user, so the client can show it directly.
 */
public class HotelException extends Exception {
    private static final long serialVersionUID = 1L;

    public HotelException(String message) {
        super(message);
    }
}
