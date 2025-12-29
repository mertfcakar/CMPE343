package com.group12.greengrocer.database;

import com.group12.greengrocer.models.Message;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MessageDAO {

    // Tüm mesajları getir (Gönderen adıyla birlikte)
    public static List<Message> getAllMessages() {
        List<Message> messages = new ArrayList<>();
        // users tablosuyla birleştirip gönderen adını alıyoruz
        String sql = "SELECT m.*, u.username FROM messages m JOIN users u ON m.sender_id = u.id ORDER BY m.created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                messages.add(new Message(
                    rs.getInt("id"),
                    rs.getInt("sender_id"),
                    rs.getString("username"),
                    rs.getString("subject"),
                    rs.getString("message"),
                    rs.getTimestamp("created_at")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    // Mesaj sil
    public static boolean deleteMessage(int messageId) {
        String sql = "DELETE FROM messages WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, messageId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Mesaja cevap ver (Basitçe yeni bir mesaj oluşturur)
    public static boolean replyToMessage(int senderId, int receiverId, String subject, String content) {
        String sql = "INSERT INTO messages (sender_id, receiver_id, subject, message) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, senderId);
            ps.setInt(2, receiverId);
            ps.setString(3, "RE: " + subject);
            ps.setString(4, content);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    // Yeni Mesaj Gönder
    public static boolean sendMessage(int senderId, int receiverId, String subject, String content) {
        String sql = "INSERT INTO messages (sender_id, receiver_id, subject, message, is_read) VALUES (?, ?, ?, ?, 0)";
        try (java.sql.Connection conn = DatabaseConnection.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, senderId);
            ps.setInt(2, receiverId);
            ps.setString(3, subject);
            ps.setString(4, content);
            return ps.executeUpdate() > 0;
        } catch (java.sql.SQLException e) { return false; }
    }
}