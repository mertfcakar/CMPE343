package com.group12.greengrocer.database;

import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.group12.greengrocer.models.CartItem;
import com.group12.greengrocer.models.Order;
import com.group12.greengrocer.models.User;
import com.group12.greengrocer.utils.ShoppingCart;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class OrderDAO {

    // --- SİPARİŞ OLUŞTURMA ---
    public static boolean createOrder(User user, double subtotal, double vat, double discount, double total,
            LocalDate date, String timeSlot, String paymentMethod, double loyaltyDiscount) {

        String orderSql = "INSERT INTO orders (user_id, status, subtotal, vat_amount, discount_amount, total_cost, " +
                "order_time, requested_delivery_date, delivery_neighborhood, delivery_address, payment_method, loyalty_discount) " +
                "VALUES (?, 'pending', ?, ?, ?, ?, NOW(), ?, ?, ?, ?, ?)";

        String itemSql = "INSERT INTO order_items (order_id, product_id, product_name, quantity, unit_price, total_price) "
                +
                "VALUES (?, ?, ?, ?, ?, ?)";

        String stockCheckSql = "SELECT stock FROM products WHERE id = ? FOR UPDATE";
        String updateStockSql = "UPDATE products SET stock = stock - ? WHERE id = ?";

        String startTime = timeSlot.split(" - ")[0];
        if (startTime.length() == 4)
            startTime = "0" + startTime;

        Timestamp deliveryTs = Timestamp.valueOf(date.atTime(LocalTime.parse(startTime)));

        Connection conn = null;
        PreparedStatement psOrder = null, psItem = null, psCheck = null, psUpdateStock = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Sipariş Kaydı
            psOrder = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS);
            psOrder.setInt(1, user.getId());
            psOrder.setDouble(2, subtotal);
            psOrder.setDouble(3, vat);
            psOrder.setDouble(4, discount);
            psOrder.setDouble(5, total);
            psOrder.setTimestamp(6, deliveryTs);
            psOrder.setString(7, user.getNeighborhood());
            psOrder.setString(8, user.getAddress());
            psOrder.setString(9, paymentMethod);
            psOrder.setDouble(10, loyaltyDiscount);

            if (psOrder.executeUpdate() == 0)
                throw new SQLException("Order creation failed.");

            rs = psOrder.getGeneratedKeys();
            if (!rs.next())
                throw new SQLException("Order ID not generated.");
            int orderId = rs.getInt(1);

            psCheck = conn.prepareStatement(stockCheckSql);
            psUpdateStock = conn.prepareStatement(updateStockSql);
            psItem = conn.prepareStatement(itemSql);

            // 2. Ürünlerin Kaydı ve Stok Güncelleme
            for (CartItem item : ShoppingCart.getInstance().getItems()) {
                psCheck.setInt(1, item.getProduct().getId());
                ResultSet rsStock = psCheck.executeQuery();

                if (!rsStock.next())
                    throw new SQLException("Product not found.");
                if (rsStock.getDouble("stock") < item.getQuantity())
                    throw new SQLException("Not enough stock.");

                psItem.setInt(1, orderId);
                psItem.setInt(2, item.getProduct().getId());
                psItem.setString(3, item.getProduct().getName());
                psItem.setDouble(4, item.getQuantity());
                psItem.setDouble(5, item.getProduct().getCurrentPrice());
                psItem.setDouble(6, item.getTotalPrice());
                psItem.addBatch();

                psUpdateStock.setDouble(1, item.getQuantity());
                psUpdateStock.setInt(2, item.getProduct().getId());
                psUpdateStock.executeUpdate();
            }

            psItem.executeBatch();

            // PDF Fatura Oluştur ve Kaydet
            byte[] pdfBytes = generateInvoicePDF(orderId, user, subtotal, vat, discount, loyaltyDiscount, total, ShoppingCart.getInstance().getItems());
            String updatePdfSql = "UPDATE orders SET invoice = ? WHERE id = ?";
            try (PreparedStatement psPdf = conn.prepareStatement(updatePdfSql)) {
                psPdf.setBytes(1, pdfBytes);
                psPdf.setInt(2, orderId);
                psPdf.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            try {
                if (conn != null)
                    conn.rollback();
            } catch (SQLException ignored) {
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (rs != null)
                    rs.close();
            } catch (Exception e) {
            }
            try {
                if (psOrder != null)
                    psOrder.close();
            } catch (Exception e) {
            }
            try {
                if (psItem != null)
                    psItem.close();
            } catch (Exception e) {
            }
            try {
                if (psCheck != null)
                    psCheck.close();
            } catch (Exception e) {
            }
            try {
                if (psUpdateStock != null)
                    psUpdateStock.close();
            } catch (Exception e) {
            }
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (Exception e) {
            }
        }
    }

    // --- KURYE PUANLAMA SİSTEMİ ---
    public static boolean rateCarrier(int orderId, int carrierId, int customerId, int rating, String comment) {
        String checkSql = "SELECT id FROM carrier_ratings WHERE order_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return false; // Zaten oylanmış
            }
        } catch (SQLException e) {
            return false;
        }

        String sql = "INSERT INTO carrier_ratings (order_id, carrier_id, customer_id, rating, comment) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.setInt(2, carrierId);
            ps.setInt(3, customerId);
            ps.setInt(4, rating);
            ps.setString(5, comment);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static double getCarrierAverageRating(int carrierId) {
        String sql = "SELECT AVG(rating) FROM carrier_ratings WHERE carrier_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, carrierId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public static boolean isOrderRated(int orderId) {
        String sql = "SELECT COUNT(*) FROM carrier_ratings WHERE order_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // --- KURYE DASHBOARD VE YÖNETİMİ ---
    public static List<Order> getCarrierDashboardOrders(int carrierId, String neighborhood) {
        List<Order> orders = new ArrayList<>();
        boolean isAllRegions = neighborhood == null || neighborhood.equalsIgnoreCase("All")
                || neighborhood.equalsIgnoreCase("Tüm İstanbul");

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT o.*, u.username AS customer_name FROM orders o ")
                .append("JOIN users u ON o.user_id = u.id WHERE (")
                .append("(o.status = 'pending' AND (o.carrier_id IS NULL OR o.carrier_id = 0)) ")
                .append("OR (o.carrier_id = ? AND o.status = 'assigned') ")
                .append("OR (o.carrier_id = ? AND o.status = 'completed' AND o.delivery_time >= DATE_SUB(NOW(), INTERVAL 30 DAY))")
                .append(") ");

        if (!isAllRegions)
            sql.append(" AND o.delivery_neighborhood = ? ");
        sql.append(" ORDER BY o.priority_level DESC, o.order_time ASC");

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setInt(1, carrierId);
            ps.setInt(2, carrierId);
            if (!isAllRegions)
                ps.setString(3, neighborhood);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    orders.add(mapResultSetToOrder(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public static List<String> getOrderItemsAsText(int orderId) {
        List<String> items = new ArrayList<>();
        String sql = "SELECT product_name, quantity FROM order_items WHERE order_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    items.add(String.format("- %.2f kg %s", rs.getDouble("quantity"), rs.getString("product_name")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    public static boolean assignAndPickUp(int orderId, int carrierId) {
        return executeUpdate(
                "UPDATE orders SET carrier_id = ?, status = 'assigned' WHERE id = ? AND status = 'pending'", carrierId,
                orderId);
    }

    public static boolean releaseOrderToPool(int orderId, int carrierId) {
        return executeUpdate(
                "UPDATE orders SET carrier_id = NULL, status = 'pending' WHERE id = ? AND carrier_id = ? AND status = 'assigned'",
                orderId, carrierId);
    }

    public static boolean hasActiveOrders(int carrierId) {
        String sql = "SELECT COUNT(*) FROM orders WHERE carrier_id = ? AND status != 'completed'";
        try (Connection con = DatabaseConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, carrierId);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getInt(1) > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean completeOrder(int orderId, int carrierId, LocalDateTime deliveryDateTime) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "UPDATE orders SET status = 'completed', delivery_time = ? WHERE id = ? AND carrier_id = ? AND status = 'assigned'")) {
            ps.setTimestamp(1, Timestamp.valueOf(deliveryDateTime));
            ps.setInt(2, orderId);
            ps.setInt(3, carrierId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean undoCompleteOrder(int orderId, int carrierId) {
        return executeUpdate(
                "UPDATE orders SET status = 'assigned', delivery_time = NULL WHERE id = ? AND carrier_id = ? AND status = 'completed'",
                orderId, carrierId);
    }

    // --- OWNER RAPORLARI ---
    public static List<Order> getAllOrdersForAdmin() {
        return getList(
                "SELECT o.*, u.username AS customer_name FROM orders o JOIN users u ON o.user_id = u.id ORDER BY o.order_time DESC");
    }

    public static double getTotalRevenue() {
        try (Connection conn = DatabaseConnection.getConnection();
                ResultSet rs = conn.createStatement()
                        .executeQuery("SELECT SUM(total_cost) FROM orders WHERE status = 'completed'")) {
            if (rs.next())
                return rs.getDouble(1);
        } catch (SQLException e) {
        }
        return 0.0;
    }

    public static int getActiveOrderCount() {
        try (Connection conn = DatabaseConnection.getConnection();
                ResultSet rs = conn.createStatement()
                        .executeQuery("SELECT COUNT(*) FROM orders WHERE status IN ('pending', 'assigned')")) {
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
        }
        return 0;
    }

    public static Map<String, Double> getRevenueByProductReport() {
        Map<String, Double> result = new HashMap<>();

        String sql = """
            SELECT p.name, SUM(oi.quantity * oi.price) AS revenue
            FROM order_items oi
            JOIN products p ON oi.product_id = p.id
            GROUP BY p.name
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                result.put(
                    rs.getString("name"),
                    rs.getDouble("revenue")
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }


    public static Map<String, Integer> getCarrierPerformanceReport() {
        Map<String, Integer> result = new HashMap<>();

        String sql = """
            SELECT u.username, COUNT(o.id) AS completed_count
            FROM orders o
            JOIN users u ON o.carrier_id = u.id
            WHERE o.status = 'Completed'
            GROUP BY u.username
        """;

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                result.put(
                    rs.getString("username"),
                    rs.getInt("completed_count")
                );
            }

        } catch (Exception e) {
        
        }

        return result;
    }

    // --- EN ÇOK SATILAN ÜRÜNLER (MİKTAR BAZLI) ---
    public static Map<String, Double> getMostSoldProducts(int limit) {
        Map<String, Double> data = new HashMap<>();
        String sql = "SELECT p.name, SUM(oi.quantity) as total_quantity " +
                     "FROM order_items oi " +
                     "JOIN products p ON oi.product_id = p.id " +
                     "JOIN orders o ON oi.order_id = o.id " +
                     "WHERE o.status = 'completed' " +
                     "GROUP BY p.name " +
                     "ORDER BY total_quantity DESC " +
                     "LIMIT ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    data.put(rs.getString("name"), rs.getDouble("total_quantity"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    // --- EN AKTİF MÜŞTERİLER ---
    public static Map<String, Integer> getMostActiveCustomers(int limit) {
        Map<String, Integer> data = new HashMap<>();
        String sql = "SELECT u.username, COUNT(o.id) as order_count " +
                     "FROM orders o " +
                     "JOIN users u ON o.user_id = u.id " +
                     "WHERE o.status = 'completed' " +
                     "GROUP BY u.username " +
                     "ORDER BY order_count DESC " +
                     "LIMIT ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    data.put(rs.getString("username"), rs.getInt("order_count"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    // --- SİPARİŞ YOĞUNLUĞU (SAAT BAZLI) ---
    public static Map<String, Integer> getOrderIntensityByHour() {
        Map<String, Integer> data = new HashMap<>();
        String sql = "SELECT HOUR(order_time) as hour, COUNT(*) as order_count " +
                     "FROM orders " +
                     "WHERE status = 'completed' " +
                     "GROUP BY HOUR(order_time) " +
                     "ORDER BY hour";
        try (Connection conn = DatabaseConnection.getConnection();
             ResultSet rs = conn.createStatement().executeQuery(sql)) {
            while (rs.next()) {
                int hour = rs.getInt("hour");
                data.put(String.format("%02d:00", hour), rs.getInt("order_count"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    // --- SİPARİŞ YOĞUNLUĞU (GÜN BAZLI) ---
    public static Map<String, Integer> getOrderIntensityByDay() {
        Map<String, Integer> data = new HashMap<>();
        String sql = "SELECT DAYNAME(order_time) as day_name, COUNT(*) as order_count " +
                     "FROM orders " +
                     "WHERE status = 'completed' " +
                     "GROUP BY DAYNAME(order_time) " +
                     "ORDER BY FIELD(DAYNAME(order_time), 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday')";
        try (Connection conn = DatabaseConnection.getConnection();
             ResultSet rs = conn.createStatement().executeQuery(sql)) {
            while (rs.next()) {
                data.put(rs.getString("day_name"), rs.getInt("order_count"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    // --- CARRIER ORTALAMA PERFORMANSI (RATING BAZLI) ---
    public static Map<String, Double> getCarrierAverageRatings() {
        Map<String, Double> data = new HashMap<>();
        String sql = "SELECT u.username, AVG(cr.rating) as avg_rating " +
                     "FROM carrier_ratings cr " +
                     "JOIN users u ON cr.carrier_id = u.id " +
                     "GROUP BY u.username " +
                     "ORDER BY avg_rating DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             ResultSet rs = conn.createStatement().executeQuery(sql)) {
            while (rs.next()) {
                data.put(rs.getString("username"), rs.getDouble("avg_rating"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    // --- KATEGORİ BAZLI GELİR (VEGETABLE vs FRUIT) ---
    public static Map<String, Double> getRevenueByCategory() {
        Map<String, Double> data = new HashMap<>();
        String sql = "SELECT p.type, SUM(oi.total_price) as total_revenue " +
                     "FROM order_items oi " +
                     "JOIN products p ON oi.product_id = p.id " +
                     "JOIN orders o ON oi.order_id = o.id " +
                     "WHERE o.status = 'completed' " +
                     "GROUP BY p.type";
        try (Connection conn = DatabaseConnection.getConnection();
             ResultSet rs = conn.createStatement().executeQuery(sql)) {
            while (rs.next()) {
                data.put(rs.getString("type"), rs.getDouble("total_revenue"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    // --- ZAMAN BAZLI RAPORLAR ---
    public static Map<String, Double> getRevenueByTimeReport(String period) {
        Map<String, Double> data = new HashMap<>();
        String sql;
        
        switch (period.toLowerCase()) {
            case "daily":
                sql = "SELECT DATE(order_time) as period, SUM(total_cost) as revenue " +
                      "FROM orders WHERE status = 'completed' " +
                      "GROUP BY DATE(order_time) ORDER BY period DESC LIMIT 30";
                break;
            case "weekly":
                sql = "SELECT YEARWEEK(order_time) as period, SUM(total_cost) as revenue " +
                      "FROM orders WHERE status = 'completed' " +
                      "GROUP BY YEARWEEK(order_time) ORDER BY period DESC LIMIT 12";
                break;
            case "monthly":
                sql = "SELECT DATE_FORMAT(order_time, '%Y-%m') as period, SUM(total_cost) as revenue " +
                      "FROM orders WHERE status = 'completed' " +
                      "GROUP BY DATE_FORMAT(order_time, '%Y-%m') ORDER BY period DESC LIMIT 12";
                break;
            default:
                sql = "SELECT DATE(order_time) as period, SUM(total_cost) as revenue " +
                      "FROM orders WHERE status = 'completed' " +
                      "GROUP BY DATE(order_time) ORDER BY period DESC LIMIT 30";
        }
        
        try (Connection conn = DatabaseConnection.getConnection();
                ResultSet rs = conn.createStatement().executeQuery(sql)) {
            while (rs.next()) {
                data.put(rs.getString("period"), rs.getDouble("revenue"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    // --- PARA BAZLI RAPORLAR ---
    public static Map<String, Double> getRevenueByAmountRange() {
        Map<String, Double> data = new HashMap<>();
        String sql = "SELECT " +
                      "CASE " +
                      "  WHEN total_cost < 100 THEN '0-100 TL' " +
                      "  WHEN total_cost < 200 THEN '100-200 TL' " +
                      "  WHEN total_cost < 300 THEN '200-300 TL' " +
                      "  WHEN total_cost < 500 THEN '300-500 TL' " +
                      "  ELSE '500+ TL' " +
                      "END as range, " +
                      "SUM(total_cost) as revenue " +
                      "FROM orders WHERE status = 'completed' " +
                      "GROUP BY range ORDER BY revenue DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
                ResultSet rs = conn.createStatement().executeQuery(sql)) {
            while (rs.next()) {
                data.put(rs.getString("range"), rs.getDouble("revenue"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    // --- MÜŞTERİ GEÇMİŞİ ---
    public static List<Order> getOrdersByUserId(int userId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE user_id = ? ORDER BY order_time DESC";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    orders.add(mapResultSetToOrder(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    // --- YENİ EKLENEN KISIM (HATAYI ÇÖZEN YER) ---
    // Resimli Sipariş Detaylarını Getir
    public static List<OrderDetail> getOrderDetailsWithImages(int orderId) {
        List<OrderDetail> details = new ArrayList<>();
        String sql = "SELECT oi.product_name, oi.quantity, oi.unit_price, oi.total_price, p.image " +
                "FROM order_items oi " +
                "LEFT JOIN products p ON oi.product_id = p.id " +
                "WHERE oi.order_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    byte[] imgBytes = null;
                    try {
                        java.sql.Blob blob = rs.getBlob("image");
                        if (blob != null) {
                            imgBytes = blob.getBytes(1, (int) blob.length());
                        }
                    } catch (Exception e) {
                    }

                    details.add(new OrderDetail(
                            rs.getString("product_name"),
                            rs.getDouble("quantity"),
                            rs.getDouble("unit_price"),
                            rs.getDouble("total_price"),
                            imgBytes));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return details;
    }

    // Bu sınıf CustomerController tarafından kullanılacak
    public static class OrderDetail {
        public String name;
        public double quantity;
        public double unitPrice;
        public double totalPrice;
        public byte[] image;

        public OrderDetail(String name, double quantity, double unitPrice, double totalPrice, byte[] image) {
            this.name = name;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.totalPrice = totalPrice;
            this.image = image;
        }
    }

    // --- YARDIMCILAR ---
    private static boolean executeUpdate(String sql, int p1, int p2) {
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, p1);
            ps.setInt(2, p2);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    private static List<Order> getList(String sql) {
        List<Order> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
                ResultSet rs = conn.createStatement().executeQuery(sql)) {
            while (rs.next())
                list.add(mapResultSetToOrder(rs));
        } catch (SQLException e) {
        }
        return list;
    }

    private static Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setId(rs.getInt("id"));
        try {
            order.setCustomerName(rs.getString("customer_name"));
        } catch (Exception e) {
        }
        order.setDeliveryNeighborhood(rs.getString("delivery_neighborhood"));
        order.setDeliveryAddress(rs.getString("delivery_address"));
        order.setStatus(rs.getString("status"));
        try {
            order.setPriorityLevel(rs.getInt("priority_level"));
        } catch (Exception e) {
        }
        order.setTotalCost(rs.getDouble("total_cost"));
        order.setOrderTime(rs.getTimestamp("order_time"));
        order.setDeliveryTime(rs.getTimestamp("delivery_time"));
        order.setRequestedDeliveryDate(rs.getTimestamp("requested_delivery_date"));
        try {
            order.setPaymentMethod(rs.getString("payment_method"));
        } catch (Exception e) {
            order.setPaymentMethod("CASH");
        }
        int cId = rs.getInt("carrier_id");
        order.setCarrierId(rs.wasNull() ? null : cId);
        return order;
    }

    // --- LOYALTY İÇİN TAMAMLANMIŞ SİPARİŞ SAYISI ---
    public static int getCompletedOrderCount(int userId) {
        String sql = "SELECT COUNT(*) FROM orders WHERE user_id = ? AND status = 'completed'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // --- PDF FATURA OLUŞTUR ---
    private static byte[] generateInvoicePDF(int orderId, User user, double subtotal, double vat, double discount, double loyalty, double total, List<CartItem> items) {
        Document document = new Document();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            // Başlık
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24, BaseColor.GREEN);
            Paragraph title = new Paragraph("GREEN GROCER", titleFont);
            title.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(title);

            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 16, BaseColor.DARK_GRAY);
            Paragraph subtitle = new Paragraph("Taze Ürünler • Hızlı Teslimat", subtitleFont);
            subtitle.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(subtitle);

            document.add(new Paragraph(" "));

            // Fatura Bilgileri
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.BLACK);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.BLACK);

            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setWidths(new float[]{1, 2});

            // Sol taraf - Fatura Bilgileri
            PdfPCell cell = new PdfPCell(new Phrase("FATURA BİLGİLERİ", headerFont));
            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            cell.setColspan(2);
            infoTable.addCell(cell);

            infoTable.addCell(new Phrase("Fatura No:", normalFont));
            infoTable.addCell(new Phrase("#" + orderId, normalFont));

            infoTable.addCell(new Phrase("Tarih:", normalFont));
            infoTable.addCell(new Phrase(LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), normalFont));

            // Sağ taraf - Müşteri Bilgileri
            cell = new PdfPCell(new Phrase("MÜŞTERİ BİLGİLERİ", headerFont));
            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            cell.setColspan(2);
            infoTable.addCell(cell);

            infoTable.addCell(new Phrase("Ad Soyad:", normalFont));
            infoTable.addCell(new Phrase(user.getUsername(), normalFont));

            infoTable.addCell(new Phrase("Adres:", normalFont));
            infoTable.addCell(new Phrase(user.getAddress() + ", " + user.getNeighborhood(), normalFont));

            infoTable.addCell(new Phrase("Telefon:", normalFont));
            infoTable.addCell(new Phrase(user.getPhoneNumber(), normalFont));

            document.add(infoTable);
            document.add(new Paragraph(" "));

            // Ürünler Tablosu
            PdfPTable productTable = new PdfPTable(4);
            productTable.setWidthPercentage(100);
            productTable.setWidths(new float[]{4, 1, 2, 2});

            // Tablo Başlıkları
            Font tableHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE);
            cell = new PdfPCell(new Phrase("Ürün", tableHeaderFont));
            cell.setBackgroundColor(BaseColor.DARK_GRAY);
            productTable.addCell(cell);

            cell = new PdfPCell(new Phrase("Miktar", tableHeaderFont));
            cell.setBackgroundColor(BaseColor.DARK_GRAY);
            productTable.addCell(cell);

            cell = new PdfPCell(new Phrase("Birim Fiyat", tableHeaderFont));
            cell.setBackgroundColor(BaseColor.DARK_GRAY);
            productTable.addCell(cell);

            cell = new PdfPCell(new Phrase("Toplam", tableHeaderFont));
            cell.setBackgroundColor(BaseColor.DARK_GRAY);
            productTable.addCell(cell);

            // Ürün Satırları
            boolean alternate = false;
            for (CartItem item : items) {
                BaseColor rowColor = alternate ? BaseColor.WHITE : new BaseColor(245, 245, 245);
                alternate = !alternate;

                cell = new PdfPCell(new Phrase(item.getProduct().getName(), normalFont));
                cell.setBackgroundColor(rowColor);
                productTable.addCell(cell);

                cell = new PdfPCell(new Phrase(String.format("%.2f kg", item.getQuantity()), normalFont));
                cell.setBackgroundColor(rowColor);
                productTable.addCell(cell);

                cell = new PdfPCell(new Phrase(String.format("%.2f TL", item.getProduct().getCurrentPrice()), normalFont));
                cell.setBackgroundColor(rowColor);
                productTable.addCell(cell);

                cell = new PdfPCell(new Phrase(String.format("%.2f TL", item.getTotalPrice()), normalFont));
                cell.setBackgroundColor(rowColor);
                productTable.addCell(cell);
            }

            document.add(productTable);
            document.add(new Paragraph(" "));

            // Toplam Tablosu
            PdfPTable totalTable = new PdfPTable(2);
            totalTable.setWidthPercentage(50);
            totalTable.setHorizontalAlignment(PdfPTable.ALIGN_RIGHT);
            totalTable.setWidths(new float[]{2, 2});

            Font totalFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.BLACK);
            Font totalBoldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.BLACK);

            totalTable.addCell(new Phrase("Ara Toplam:", totalFont));
            totalTable.addCell(new Phrase(String.format("%.2f TL", subtotal), totalFont));

            totalTable.addCell(new Phrase("KDV (%18):", totalFont));
            totalTable.addCell(new Phrase(String.format("%.2f TL", vat), totalFont));

            if (discount > 0) {
                totalTable.addCell(new Phrase("İndirim:", totalFont));
                totalTable.addCell(new Phrase(String.format("-%.2f TL", discount), totalFont));
            }

            if (loyalty > 0) {
                totalTable.addCell(new Phrase("Sadakat İndirimi:", totalFont));
                totalTable.addCell(new Phrase(String.format("-%.2f TL", loyalty), totalFont));
            }

            // Toplam satırı
            cell = new PdfPCell(new Phrase("GENEL TOPLAM:", totalBoldFont));
            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            totalTable.addCell(cell);

            cell = new PdfPCell(new Phrase(String.format("%.2f TL", total), totalBoldFont));
            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            totalTable.addCell(cell);

            document.add(totalTable);
            document.add(new Paragraph(" "));

            // Footer
            Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.GRAY);
            Paragraph footer = new Paragraph("Teşekkür ederiz! Siparişiniz için GreenGrocer'ı tercih ettiğiniz için.", footerFont);
            footer.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(footer);

            Paragraph footer2 = new Paragraph("Bu fatura elektronik ortamda oluşturulmuştur.", footerFont);
            footer2.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(footer2);

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return baos.toByteArray();
    }

    // --- SİPARİŞ İPTALİ ---
    public static boolean cancelOrder(int orderId) {
        String updateStatusSql = "UPDATE orders SET status = 'cancelled' WHERE id = ? AND status = 'pending'";
        String getItemsSql = "SELECT product_id, quantity FROM order_items WHERE order_id = ?";
        String updateStockSql = "UPDATE products SET stock = stock + ? WHERE id = ?";

        Connection conn = null;
        PreparedStatement psStatus = null, psItems = null, psStock = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // Status güncelle
            psStatus = conn.prepareStatement(updateStatusSql);
            psStatus.setInt(1, orderId);
            if (psStatus.executeUpdate() == 0) {
                return false; // Zaten iptal edilmiş veya pending değil
            }

            // Ürünleri al ve stok geri yükle
            psItems = conn.prepareStatement(getItemsSql);
            psItems.setInt(1, orderId);
            rs = psItems.executeQuery();

            psStock = conn.prepareStatement(updateStockSql);
            while (rs.next()) {
                int productId = rs.getInt("product_id");
                double quantity = rs.getDouble("quantity");
                psStock.setDouble(1, quantity);
                psStock.setInt(2, productId);
                psStock.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ignored) {}
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (rs != null) rs.close();
                if (psStatus != null) psStatus.close();
                if (psItems != null) psItems.close();
                if (psStock != null) psStock.close();
                if (conn != null) conn.close();
            } catch (SQLException ignored) {}
        }
    }

    // --- SİPARİŞİ DEĞERLENDİR ---
    public static boolean rateOrder(int orderId, int rating) {
        String getOrderSql = "SELECT user_id, carrier_id FROM orders WHERE id = ?";
        String insertRatingSql = "INSERT INTO carrier_ratings (order_id, carrier_id, customer_id, rating) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement getStmt = conn.prepareStatement(getOrderSql);
             PreparedStatement insertStmt = conn.prepareStatement(insertRatingSql)) {
            getStmt.setInt(1, orderId);
            ResultSet rs = getStmt.executeQuery();
            if (rs.next()) {
                int userId = rs.getInt("user_id");
                int carrierId = rs.getInt("carrier_id");
                if (carrierId == 0) return false; // No carrier assigned
                insertStmt.setInt(1, orderId);
                insertStmt.setInt(2, carrierId);
                insertStmt.setInt(3, userId);
                insertStmt.setInt(4, rating);
                return insertStmt.executeUpdate() > 0;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Order details'te PDF butonu ekleme
    public static byte[] getInvoicePDF(int orderId) {
        String sql = "SELECT invoice FROM orders WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBytes("invoice");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // --- KURYE DEĞERLENDİRMELERİNİ GETİR ---
    public static class CarrierRating {
        public int orderId;
        public String customerName;
        public int rating;
        public String comment;
        public LocalDateTime createdAt;

        public CarrierRating(int orderId, String customerName, int rating, String comment, LocalDateTime createdAt) {
            this.orderId = orderId;
            this.customerName = customerName;
            this.rating = rating;
            this.comment = comment;
            this.createdAt = createdAt;
        }
    }

    public static List<CarrierRating> getCarrierRatings(int carrierId) {
        String sql = "SELECT cr.order_id, u.username as customer_name, cr.rating, cr.comment, cr.created_at " +
                     "FROM carrier_ratings cr " +
                     "JOIN users u ON cr.customer_id = u.id " +
                     "WHERE cr.carrier_id = ? " +
                     "ORDER BY cr.created_at DESC";
        List<CarrierRating> ratings = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, carrierId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ratings.add(new CarrierRating(
                        rs.getInt("order_id"),
                        rs.getString("customer_name"),
                        rs.getInt("rating"),
                        rs.getString("comment"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ratings;
    }
}