package com.group12.greengrocer.database;

import com.group12.greengrocer.models.Message;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageDAO {

    // 1. TÜM MESAJLARI GETİR (Owner Paneli İçin)
    public static List<Message> getAllMessages() {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT m.*, u.username FROM messages m JOIN users u ON m.sender_id = u.id ORDER BY m.created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                messages.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    // 2. MESAJ SİL
    public static boolean deleteMessage(int messageId) {
        String sql = "DELETE FROM messages WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, messageId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    // 3. CEVAP VER
    public static boolean replyToMessage(int senderId, int receiverId, String subject, String content) {
        return sendMessage(senderId, receiverId, "RE: " + subject, content);
    }

    // İki kullanıcı arasındaki sohbet (Customer Chat)
    public static List<Message> getConversation(int userId1, int userId2) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT m.*, u.username FROM messages m " +
                "JOIN users u ON m.sender_id = u.id " +
                "WHERE (m.sender_id = ? AND m.receiver_id = ?) " +
                "   OR (m.sender_id = ? AND m.receiver_id = ?) " +
                "ORDER BY m.created_at ASC";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId1);
            ps.setInt(2, userId2);
            ps.setInt(3, userId2);
            ps.setInt(4, userId1);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    messages.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    // Ticket Durumlarını Getir
    public static Map<String, String> getTicketStatuses(int userId) {
        Map<String, String> tickets = new HashMap<>();
        String sql = "SELECT subject, status FROM messages WHERE sender_id = ? OR receiver_id = ? GROUP BY subject, status";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    tickets.put(rs.getString("subject"), rs.getString("status"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tickets;
    }

    // Mesaj Gönder
    public static boolean sendMessage(int senderId, int receiverId, String subject, String content) {
        // Yeni mesaj atıldığında, konu durumu otomatik olarak 'OPEN' olsun.
        updateTicketStatus(subject, "OPEN");

        String sql = "INSERT INTO messages (sender_id, receiver_id, subject, message, is_read, created_at, status) VALUES (?, ?, ?, ?, 0, NOW(), 'OPEN')";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, senderId);
            ps.setInt(2, receiverId);
            ps.setString(3, subject);
            ps.setString(4, content);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }
    public static boolean deleteChatTopic(int userId, String subject) {
        // Hem gönderilen hem alınan, bu konuya ait tüm mesajları sil
        String sql = "DELETE FROM messages WHERE (sender_id = ? OR receiver_id = ?) AND subject = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            ps.setString(3, subject);
            
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateTicketStatus(String subject, String status) {
        String sql = "UPDATE messages SET status = ? WHERE subject = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, subject);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    private static Message mapRow(ResultSet rs) throws SQLException {
        return new Message(
                rs.getInt("id"),
                rs.getInt("sender_id"),
                rs.getString("username"),
                rs.getString("subject"),
                rs.getString("message"),
                rs.getTimestamp("created_at"));
    }
}