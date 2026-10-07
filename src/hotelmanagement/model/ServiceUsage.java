package hotelmanagement.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/** One row of "service_usage": a service used by a booking. */
public class ServiceUsage implements Serializable {
    private static final long serialVersionUID = 1L;

    private int usageId;
    private int bookingId;
    private int serviceId;
    private String serviceName;
    private double price;       // price of one unit
    private int quantity;
    private LocalDateTime usageDate;

    public ServiceUsage() {
    }

    /** Charge for this line = unit price x quantity. */
    public double getAmount() {
        return price * quantity;
    }

    public int getUsageId() {
        return usageId;
    }

    public void setUsageId(int usageId) {
        this.usageId = usageId;
    }

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public LocalDateTime getUsageDate() {
        return usageDate;
    }

    public void setUsageDate(LocalDateTime usageDate) {
        this.usageDate = usageDate;
    }
}
