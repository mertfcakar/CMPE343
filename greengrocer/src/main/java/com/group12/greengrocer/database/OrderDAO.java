package com.group12.greengrocer.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.group12.greengrocer.models.Order;

public class OrderDAO {

    /**
     * Kurye Paneli için Dashboard Verilerini Getirir
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

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapResultSetToOrder(rs));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return orders;
    }

    /**
     * Siparişin ürünlerini metin olarak getirir
     */
    public static List<String> getOrderItemsAsText(int orderId) {
        List<String> items = new ArrayList<>();
        String sql = "SELECT product_name, quantity FROM order_items WHERE order_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString("product_name");
                    double qty = rs.getDouble("quantity");
                    items.add(String.format("- %.2f kg %s", qty, name));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    public static boolean assignAndPickUp(int orderId, int carrierId) {
        String sql = "UPDATE orders SET carrier_id = ?, status = 'assigned' WHERE id = ? AND status = 'pending'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, carrierId);
            ps.setInt(2, orderId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public static boolean releaseOrderToPool(int orderId, int carrierId) {
        String sql = "UPDATE orders SET carrier_id = NULL, status = 'pending' WHERE id = ? AND carrier_id = ? AND status = 'assigned'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.setInt(2, carrierId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public static boolean completeOrder(int orderId, int carrierId, java.time.LocalDateTime deliveryDateTime) {
        Timestamp timestamp = Timestamp.valueOf(deliveryDateTime);
        String sql = "UPDATE orders SET status = 'completed', delivery_time = ? WHERE id = ? AND carrier_id = ? AND status = 'assigned'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, timestamp);
            ps.setInt(2, orderId);
            ps.setInt(3, carrierId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    /**
     * [YENİ]: Tamamlanmış siparişi geri al (Undo)
     * Status: completed -> assigned
     * Delivery Time: NULL yapılır
     */
    public static boolean undoCompleteOrder(int orderId, int carrierId) {
        String sql = "UPDATE orders SET status = 'assigned', delivery_time = NULL WHERE id = ? AND carrier_id = ? AND status = 'completed'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.setInt(2, carrierId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

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