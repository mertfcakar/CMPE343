package com.group12.greengrocer.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.group12.greengrocer.models.Order;

/**
 * Data Access Object for Order operations
 */
public class OrderDAO {

    // --- SENİN KODUNLA UYUMLU KURYE PANELİ METODLARI ---

    /**
     * Dashboard verilerini getirir (Hüseyin'in Kurye Paneli için)
     */
    public static List<Order> getCarrierDashboardOrders(int carrierId, String neighborhood) {
        List<Order> orders = new ArrayList<>();
        boolean isAllRegions = neighborhood == null || 
                               neighborhood.equalsIgnoreCase("All") || 
                               neighborhood.equalsIgnoreCase("Tüm İstanbul");

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT o.*, u.username AS customer_name FROM orders o ")
           .append("JOIN users u ON o.user_id = u.id WHERE (")
           .append("(o.status = 'pending' AND (o.carrier_id IS NULL OR o.carrier_id = 0)) ")
           .append("OR (o.carrier_id = ? AND o.status = 'assigned') ")
           .append("OR (o.carrier_id = ? AND o.status = 'completed' AND o.delivery_time >= DATE_SUB(NOW(), INTERVAL 30 DAY))")
           .append(") ");

        if (!isAllRegions) {
            sql.append(" AND o.delivery_neighborhood = ? ");
        }
        sql.append(" ORDER BY o.priority_level DESC, o.order_time ASC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setInt(1, carrierId);
            ps.setInt(2, carrierId);
            if (!isAllRegions) ps.setString(3, neighborhood);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                orders.add(mapResultSetToOrder(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return orders;
    }

    /**
     * Siparişi üstüne al: pending -> assigned
     */
    public static boolean assignAndPickUp(int orderId, int carrierId) {
        String sql = "UPDATE orders SET carrier_id = ?, status = 'assigned' WHERE id = ? AND status = 'pending'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, carrierId);
            ps.setInt(2, orderId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    /**
     * Siparişi havuza geri bırak: assigned -> pending
     */
    public static boolean releaseOrderToPool(int orderId, int carrierId) {
        String sql = "UPDATE orders SET carrier_id = NULL, status = 'pending' WHERE id = ? AND carrier_id = ? AND status = 'assigned'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.setInt(2, carrierId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    /**
     * Siparişi tamamla: assigned -> completed
     */
    public static boolean completeOrder(int orderId, int carrierId) {
        String sql = "UPDATE orders SET status = 'completed', delivery_time = CURRENT_TIMESTAMP WHERE id = ? AND carrier_id = ? AND status = 'assigned'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.setInt(2, carrierId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // --- ARKADAŞININ TASLAKLARI (ŞİMDİLİK TODO OLARAK KALANLAR) ---

    public static int createOrder(Order order) {
        // TODO: Implement
        return 0;
    }

    public static List<Order> getOrdersByCustomer(int userId) {
        // TODO: Implement
        return new ArrayList<>();
    }

    public static List<Order> getAvailableOrders() {
        // Hüseyin: Bu metot yukarıdaki getCarrierDashboardOrders ile benzer işi yapıyor.
        return new ArrayList<>();
    }

    public static List<Order> getOrdersByCarrier(int carrierId) {
        // TODO: Implement
        return new ArrayList<>();
    }

    public static List<Order> getAllOrders() {
        // TODO: Implement
        return new ArrayList<>();
    }

    public static boolean cancelOrder(int orderId) {
        // TODO: Implement
        return false;
    }

    public static boolean updateOrderStatus(int orderId, String status) {
        // TODO: Implement
        return false;
    }

    // --- YARDIMCI METOT (ResultSet -> Model Dönüşümü) ---

    private static Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setId(rs.getInt("id"));
        try { order.setCustomerName(rs.getString("customer_name")); } catch (Exception e) {}
        order.setDeliveryNeighborhood(rs.getString("delivery_neighborhood"));
        order.setDeliveryAddress(rs.getString("delivery_address"));
        order.setStatus(rs.getString("status"));
        order.setPriorityLevel(rs.getInt("priority_level"));
        order.setTotalCost(rs.getDouble("total_cost"));
        order.setOrderTime(rs.getTimestamp("order_time"));
        order.setDeliveryTime(rs.getTimestamp("delivery_time"));
        
        int cId = rs.getInt("carrier_id");
        order.setCarrierId(rs.wasNull() ? null : cId);
        return order;
    }
}