package com.group12.greengrocer.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.group12.greengrocer.models.User;

public class UserDAO {

    /**
     * Kullanıcı girişi: BINARY kullanarak harf duyarlılığını zorunlu kılıyoruz.
     * Veritabanındaki tüm kullanıcı bilgilerini (adres, semt dahil) çeker.
     */
    public static User login(String username, String password) {
        String sql = "SELECT * FROM users WHERE BINARY username = ? AND BINARY password = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, username);
            ps.setString(2, password);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // User modelindeki constructor sırasına göre nesne oluşturuluyor
                    return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role"),
                        rs.getString("address"),          // DB: address
                        rs.getString("contact_details"),  // DB: contact_details
                        rs.getString("neighborhood")       // DB: neighborhood
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Login İşlemi Sırasında Hata: " + e.getMessage());
        }
        return null;
    }

    /**
     * Şifre değiştirirken eski şifre kontrolü için kullanılır.
     */
    public static String getUserPassword(String username) {
        String sql = "SELECT password FROM users WHERE BINARY username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, username);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("password");
                }
            }
        } catch (SQLException e) {
            System.err.println("Şifre Bilgisi Çekilirken Hata: " + e.getMessage());
        }
        return null;
    }

    /**
     * Şifreyi günceller. Başarılıysa true döner.
     */
    public static boolean updatePassword(String username, String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE BINARY username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, newPassword);
            ps.setString(2, username);
            
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Şifre Güncelleme Hatası: " + e.getMessage());
            return false;
        }
    }

    /**
     * Kullanıcı var mı kontrolü (Kayıt veya kontrol işlemleri için).
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
}