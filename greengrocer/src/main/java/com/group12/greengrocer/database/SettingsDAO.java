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

    // Sadakat ayarlarını getir (Varsayılan değerler dönerse DB boş demektir)
    public static int[] getLoyaltySettings() {
        String sql = "SELECT min_orders, discount_percentage FROM loyalty_settings WHERE is_active = 1 LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return new int[]{rs.getInt("min_orders"), rs.getInt("discount_percentage")}; // int dönüyor ama double cast edilebilir
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return new int[]{5, 5}; // Varsayılan: 5 sipariş, %5 indirim
    }

    public static void updateLoyaltySettings(int minOrders, double discount) {
        // Önce temizle, sonra ekle (Basit mantık)
        String delSql = "DELETE FROM loyalty_settings";
        String insSql = "INSERT INTO loyalty_settings (min_orders, discount_percentage) VALUES (?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            try (PreparedStatement psDel = conn.prepareStatement(delSql)) { psDel.executeUpdate(); }
            try (PreparedStatement psIns = conn.prepareStatement(insSql)) {
                psIns.setInt(1, minOrders);
                psIns.setDouble(2, discount);
                psIns.executeUpdate();
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }
}