package com.group12.greengrocer.database;

import com.group12.greengrocer.models.Coupon;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SettingsDAO {

    // --- KUPON İŞLEMLERİ ---
    
    public static List<Coupon> getAllCoupons() {
        List<Coupon> list = new ArrayList<>();
        String sql = "SELECT * FROM coupons";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Coupon(
                    rs.getInt("id"),
                    rs.getString("code"),
                    rs.getDouble("discount_percentage"),
                    rs.getDouble("min_purchase_amount"),
                    rs.getTimestamp("valid_until"),
                    rs.getBoolean("is_active")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public static boolean addCoupon(String code, double discount, double minPurchase, java.time.LocalDate validUntil) {
        String sql = "INSERT INTO coupons (code, discount_percentage, min_purchase_amount, valid_until, is_active) VALUES (?, ?, ?, ?, 1)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            ps.setDouble(2, discount);
            ps.setDouble(3, minPurchase);
            ps.setTimestamp(4, Timestamp.valueOf(validUntil.atStartOfDay()));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // --- SADAKAT AYARLARI ---

    // Sadakat ayarlarını getir (min_orders null olabilir)
    public static Integer[] getLoyaltySettings() {
        String sql = "SELECT min_orders, discount_percentage FROM loyalty_settings WHERE is_active = 1 LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                Integer minOrders = rs.getObject("min_orders", Integer.class); // null olabilir
                double discount = rs.getDouble("discount_percentage");
                return new Integer[]{minOrders, (int)discount};
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return new Integer[]{null, 5}; // min_orders null, discount %5 varsayılan
    }

    public static void updateLoyaltySettings(Integer minOrders, double discount) {
        // Önce temizle, sonra ekle (Basit mantık)
        String delSql = "DELETE FROM loyalty_settings";
        String insSql = "INSERT INTO loyalty_settings (min_orders, discount_percentage) VALUES (?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            try (PreparedStatement psDel = conn.prepareStatement(delSql)) { psDel.executeUpdate(); }
            try (PreparedStatement psIns = conn.prepareStatement(insSql)) {
                if (minOrders != null) {
                    psIns.setInt(1, minOrders);
                } else {
                    psIns.setNull(1, java.sql.Types.INTEGER);
                }
                psIns.setDouble(2, discount);
                psIns.executeUpdate();
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // --- KUPON SİLME ---
    public static boolean deleteCoupon(int couponId) {
        String sql = "DELETE FROM coupons WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, couponId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- KUPON GÜNCELLEME ---
    public static boolean updateCoupon(int couponId, String code, double discount, double minPurchase, java.time.LocalDate validUntil, boolean isActive) {
        String sql = "UPDATE coupons SET code = ?, discount_percentage = ?, min_purchase_amount = ?, valid_until = ?, is_active = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            ps.setDouble(2, discount);
            ps.setDouble(3, minPurchase);
            ps.setTimestamp(4, Timestamp.valueOf(validUntil.atStartOfDay()));
            ps.setBoolean(5, isActive);
            ps.setInt(6, couponId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- MİNİMUM SEPET DEĞERİ ---
    public static double getMinCartValue() {
        String sql = "SELECT value FROM system_settings WHERE setting_key = 'min_cart_value'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble("value");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 50.0; // Varsayılan değer
    }

    public static boolean updateMinCartValue(double minValue) {
        String checkSql = "SELECT COUNT(*) FROM system_settings WHERE setting_key = 'min_cart_value'";
        String updateSql = "UPDATE system_settings SET value = ? WHERE setting_key = 'min_cart_value'";
        String insertSql = "INSERT INTO system_settings (setting_key, value) VALUES ('min_cart_value', ?)";
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            try (PreparedStatement psCheck = conn.prepareStatement(checkSql);
                 ResultSet rs = psCheck.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    // Update existing
                    try (PreparedStatement psUpdate = conn.prepareStatement(updateSql)) {
                        psUpdate.setDouble(1, minValue);
                        return psUpdate.executeUpdate() > 0;
                    }
                } else {
                    // Insert new
                    try (PreparedStatement psInsert = conn.prepareStatement(insertSql)) {
                        psInsert.setDouble(1, minValue);
                        return psInsert.executeUpdate() > 0;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}