package hotelmanagement.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import hotelmanagement.model.ExtraService;
import hotelmanagement.model.ServiceUsage;

/** SQL for the "services" and "service_usage" tables. */
public class ServiceDAO {

    public List<ExtraService> findAllServices(Connection con) throws SQLException {
        List<ExtraService> services = new ArrayList<>();
        String sql = "SELECT service_id, service_name, price FROM services ORDER BY service_id";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                services.add(new ExtraService(rs.getInt("service_id"), rs.getString("service_name"),
                        rs.getDouble("price")));
            }
        }
        return services;
    }

    public ExtraService findServiceById(Connection con, int serviceId) throws SQLException {
        String sql = "SELECT service_id, service_name, price FROM services WHERE service_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, serviceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new ExtraService(rs.getInt("service_id"), rs.getString("service_name"),
                            rs.getDouble("price"));
                }
                return null;
            }
        }
    }

    public void insertUsage(Connection con, int bookingId, int serviceId, int quantity) throws SQLException {
        String sql = "INSERT INTO service_usage (booking_id, service_id, quantity) VALUES (?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            ps.setInt(2, serviceId);
            ps.setInt(3, quantity);
            ps.executeUpdate();
        }
    }

    /** All services used by one booking, with the service name and unit price. */
    public List<ServiceUsage> findUsageByBooking(Connection con, int bookingId) throws SQLException {
        List<ServiceUsage> usageList = new ArrayList<>();
        String sql = "SELECT u.usage_id, u.booking_id, u.service_id, u.quantity, u.usage_date,"
                + " s.service_name, s.price"
                + " FROM service_usage u JOIN services s ON s.service_id = u.service_id"
                + " WHERE u.booking_id = ? ORDER BY u.usage_id";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ServiceUsage usage = new ServiceUsage();
                    usage.setUsageId(rs.getInt("usage_id"));
                    usage.setBookingId(rs.getInt("booking_id"));
                    usage.setServiceId(rs.getInt("service_id"));
                    usage.setQuantity(rs.getInt("quantity"));
                    usage.setUsageDate(rs.getTimestamp("usage_date").toLocalDateTime());
                    usage.setServiceName(rs.getString("service_name"));
                    usage.setPrice(rs.getDouble("price"));
                    usageList.add(usage);
                }
            }
        }
        return usageList;
    }
}
