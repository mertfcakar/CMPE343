package com.group12.greengrocer.database;

import com.group12.greengrocer.models.Message;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object (DAO) for managing Message entities.
 * <p>
 * This class handles all database operations related to the messaging system,
 * including retrieving chat histories, sending replies, managing support ticket statuses,
 * and deleting conversations.
 * </p>
 *
 * @author Group12
 * @version 1.0
 */
public class MessageDAO {

    /**
     * Retrieves all messages stored in the database.
     * <p>
     * Typically used for the Owner/Admin panel to view all system activity.
     * Results are ordered by creation date in descending order (newest first).
     * </p>
     *
     * @return A list of all {@link Message} objects with sender usernames.
     */
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

    /**
     * Deletes a single message from the database by its unique ID.
     *
     * @param messageId The unique identifier of the message to delete.
     * @return {@code true} if the deletion was successful, {@code false} otherwise.
     */
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

    /**
     * Sends a reply to an existing message.
     * <p>
     * This is a convenience wrapper around {@link #sendMessage(int, int, String, String)}.
     * It automatically prepends "RE: " to the original subject line.
     * </p>
     *
     * @param senderId   The ID of the user sending the reply.
     * @param receiverId The ID of the user receiving the reply.
     * @param subject    The original subject (without "RE:").
     * @param content    The body of the reply message.
     * @return {@code true} if the reply was successfully sent.
     */
    public static boolean replyToMessage(int senderId, int receiverId, String subject, String content) {
        return sendMessage(senderId, receiverId, "RE: " + subject, content);
    }

    /**
     * Retrieves the entire conversation history between two specific users.
     * <p>
     * Fetches messages where the user is either the sender OR the receiver,
     * effectively reconstructing the bidirectional chat log.
     * Results are ordered chronologically (oldest to newest).
     * </p>
     *
     * @param userId1 The ID of the first participant (e.g., Customer).
     * @param userId2 The ID of the second participant (e.g., Admin).
     * @return A chronological list of {@link Message} objects representing the conversation.
     */
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

    /**
     * Retrieves the current status of support tickets for a specific user.
     * <p>
     * Groups messages by subject and returns the status (e.g., "OPEN", "CLOSED")
     * associated with that subject.
     * </p>
     *
     * @param userId The ID of the user whose ticket statuses are being queried.
     * @return A Map where Key is the Subject and Value is the Status.
     */
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

    /**
     * Sends a new message and creates a database record.
     * <p>
     * <b>Side Effect:</b> Automatically updates the ticket status of the given subject to "OPEN"
     * via {@link #updateTicketStatus(String, String)} to ensure the thread is active.
     * </p>
     *
     * @param senderId   The ID of the user sending the message.
     * @param receiverId The ID of the intended recipient.
     * @param subject    The subject/topic of the message.
     * @param content    The text content of the message.
     * @return {@code true} if the message was successfully inserted into the database.
     */
    public static boolean sendMessage(int senderId, int receiverId, String subject, String content) {
        // Automatically set status to 'OPEN' when a new message is sent
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

    /**
     * Deletes all messages related to a specific subject (topic) for a user.
     * <p>
     * This is a bulk delete operation used to clear an entire conversation thread
     * from the user's view.
     * </p>
     *
     * @param userId  The ID of the user requesting the deletion.
     * @param subject The subject line of the topic to be deleted.
     * @return {@code true} if the operation was successful.
     */
    public static boolean deleteChatTopic(int userId, String subject) {
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

    /**
     * Updates the status of a conversation thread (Ticket) identified by its subject.
     *
     * @param subject The subject line identifying the conversation.
     * @param status  The new status to set (e.g., "OPEN", "CLOSED").
     * @return {@code true} if the update was successful.
     */
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

    /**
     * Maps a row from the ResultSet to a Message object.
     *
     * @param rs The ResultSet positioned at the current row.
     * @return A populated {@link Message} object.
     * @throws SQLException If a database access error occurs.
     */
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