package com.group12.greengrocer.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.group12.greengrocer.models.CartItem;
import com.group12.greengrocer.models.User;
import com.group12.greengrocer.utils.ShoppingCart;

public class UserDAO {

    /**
     * GÜNCELLENDİ: Kullanıcı girişi. Email ve Telefon sütunlarını çeker.
     */
    public static User login(String username, String password) {
        String sql = "SELECT * FROM users WHERE BINARY username = ? AND BINARY password = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, username);
            ps.setString(2, password);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role"),
                        rs.getString("address"),
                        rs.getString("email"),       // contact_details yerine email
                        rs.getString("phone_number"),// contact_details yerine phone
                        rs.getString("neighborhood")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Login İşlemi Sırasında Hata: " + e.getMessage());
        }
        return null;
    }

    /**
     * Kullanıcı var mı kontrolü (Kayıt sırasında çakışmayı önlemek için).
     */
    public static boolean isUserExists(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE BINARY username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Kullanıcı Sorgulama Hatası: " + e.getMessage());
        }
        return false;
    }

    /**
     * GÜNCELLENDİ: Müşteri Kaydı (Register). Artık Email ve Telefon ayrı ayrı alınıyor.
     */
    public static boolean registerCustomer(String username, String password, String address, String neighborhood, String email, String phone) {
        String sql = "INSERT INTO users (username, password, role, address, neighborhood, email, phone_number) VALUES (?, ?, 'customer', ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, address);
            ps.setString(4, neighborhood);
            ps.setString(5, email);
            ps.setString(6, phone);
            
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * YENİ EKLENDİ: Şifremi Unuttum (Güvenli)
     * Kullanıcı adı + Email + Telefon eşleşirse şifre değişir.
     */
    public static boolean resetPasswordSecure(String username, String email, String phone, String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE BINARY username = ? AND email = ? AND phone_number = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, newPassword);
            ps.setString(2, username);
            ps.setString(3, email);
            ps.setString(4, phone);
            
            return ps.executeUpdate() > 0; // 0 dönerse bilgiler uyuşmuyor demektir.
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- OWNER (PATRON) İŞLEMLERİ ---

    /**
     * GÜNCELLENDİ: Tüm kuryeleri getirirken yeni User yapısını kullanır.
     */
    public static List<User> getAllCarriers() {
        List<User> carriers = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role = 'carrier'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                carriers.add(new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    "carrier",
                    rs.getString("address"),
                    rs.getString("email"),
                    rs.getString("phone_number"),
                    rs.getString("neighborhood")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return carriers;
    }

    /**
     * GÜNCELLENDİ: Kurye eklerken email ve telefon zorunluluğu veritabanında olduğu için
     * bu metodu güncelledim. Eğer OwnerController'dan tek bir 'contact' geliyorsa
     * onu 'phone' kabul edip, maile geçici bir değer atayabiliriz veya o tarafı da güncelleyebiliriz.
     * Şimdilik: Email ve Phone parametresi alacak şekilde güncelledim.
     */
    public static boolean addCarrier(String username, String password, String email, String phone) {
        String sql = "INSERT INTO users (username, password, role, email, phone_number) VALUES (?, ?, 'carrier', ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, email);
            ps.setString(4, phone);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- SİPARİŞ OLUŞTURMA (SENİN ORİJİNAL KODUN) ---
    public static boolean createOrder(User user, double subtotal, double vat, double discount, double total, 
                                      LocalDate date, String timeSlot) {
        
        String orderSql = "INSERT INTO orders (user_id, status, subtotal, vat_amount, discount_amount, total_cost, " +
                          "order_time, requested_delivery_date, delivery_neighborhood, delivery_address, payment_method) " +
                          "VALUES (?, 'pending', ?, ?, ?, ?, NOW(), ?, ?, ?, 'CASH_ON_DELIVERY')";
        
        String itemSql = "INSERT INTO order_items (order_id, product_id, product_name, quantity, unit_price, total_price) " +
                         "VALUES (?, ?, ?, ?, ?, ?)";

        String startTime = timeSlot.split(" - ")[0];
        if (startTime.length() == 4) startTime = "0" + startTime; 
        
        Timestamp deliveryTs = Timestamp.valueOf(date.atTime(LocalTime.parse(startTime)));

        Connection conn = null;
        PreparedStatement psOrder = null;
        PreparedStatement psItem = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); 

            // 1. Siparişi Kaydet
            psOrder = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS);
            psOrder.setInt(1, user.getId());
            psOrder.setDouble(2, subtotal);
            psOrder.setDouble(3, vat);
            psOrder.setDouble(4, discount);
            psOrder.setDouble(5, total);
            psOrder.setTimestamp(6, deliveryTs);
            psOrder.setString(7, user.getNeighborhood());
            psOrder.setString(8, user.getAddress());
            
            int affectedRows = psOrder.executeUpdate();
            if (affectedRows == 0) throw new SQLException("Creating order failed, no rows affected.");

            int orderId = 0;
            rs = psOrder.getGeneratedKeys();
            if (rs.next()) {
                orderId = rs.getInt(1);
            } else {
                throw new SQLException("Creating order failed, no ID obtained.");
            }

            // 2. Ürünleri Kaydet
            psItem = conn.prepareStatement(itemSql);
            for (CartItem item : ShoppingCart.getInstance().getItems()) {
                psItem.setInt(1, orderId);
                psItem.setInt(2, item.getProduct().getId());
                psItem.setString(3, item.getProduct().getName());
                psItem.setDouble(4, item.getQuantity());
                psItem.setDouble(5, item.getProduct().getCurrentPrice());
                psItem.setDouble(6, item.getTotalPrice());
                psItem.addBatch();
            }
            psItem.executeBatch();

            conn.commit(); 
            return true;

        } catch (SQLException e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return false;
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (psOrder != null) psOrder.close(); } catch (SQLException e) {}
            try { if (psItem != null) psItem.close(); } catch (SQLException e) {}
            try { if (conn != null) conn.setAutoCommit(true); conn.close(); } catch (SQLException e) {}
        }
    }

    /**
     * GÜNCELLENDİ: Profil güncelleme artık mail ve telefonu ayrı günceller.
     */
    public static boolean updateUserProfile(int userId, String newAddress, String newEmail, String newPhone, String newPassword) {
        String sql = "UPDATE users SET address = ?, email = ?, phone_number = ?, password = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newAddress);
            ps.setString(2, newEmail);
            ps.setString(3, newPhone);
            ps.setString(4, newPassword);
            ps.setInt(5, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    public static int getOwnerId() {
        String sql = "SELECT id FROM users WHERE role = 'owner' LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt("id");
        } catch (SQLException e) { e.printStackTrace(); }
        return 0; 
    }
}