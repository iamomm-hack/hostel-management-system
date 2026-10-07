package hotelmanagement.model;

import java.io.Serializable;

/** An additional paid service (a row of the "services" table), e.g. Breakfast. */
public class ExtraService implements Serializable {
    private static final long serialVersionUID = 1L;

    private int serviceId;
    private String serviceName;
    private double price;

    public ExtraService() {
    }

    public ExtraService(int serviceId, String serviceName, double price) {
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.price = price;
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

    /** Shown in the service combo box on the client. */
    @Override
    public String toString() {
        return serviceName + " (Rs. " + String.format("%.2f", price) + ")";
    }
}
