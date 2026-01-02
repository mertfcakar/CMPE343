package com.group12.greengrocer.models;

import java.sql.Timestamp;

/**
 * Represents a message entity in the system.
 * <p>
 * This class is used to model communication, such as customer support tickets
 * or user feedback. It contains details about the sender, the message content,
 * and the time it was sent.
 * </p>
 */
public class Message {

    /**
     * The unique identifier for the message.
     */
    private int id;

    /**
     * The unique ID of the user who sent the message.
     */
    private int senderId;

    /**
     * The display name of the sender.
     * <p>
     * This field is typically populated via a database JOIN operation with
     * the users table when fetching the message.
     * </p>
     */
    private String senderName; 

    /**
     * The subject or title of the message.
     */
    private String subject;

    /**
     * The main body text of the message.
     */
    private String content;

    /**
     * The date and time when the message was created.
     */
    private Timestamp createdAt;

    /**
     * Indicates whether the message has been read by an administrator.
     * <p>
     * This is initialized to {@code false} by default.
     * </p>
     */
    private boolean isRead;

    /**
     * Constructs a new {@code Message} instance.
     * <p>
     * The {@code isRead} status is automatically set to {@code false} upon creation.
     * </p>
     *
     * @param id         The unique ID of the message.
     * @param senderId   The ID of the user sending the message.
     * @param senderName The name of the sender (resolved via DB join).
     * @param subject    The subject line of the message.
     * @param content    The content body of the message.
     * @param createdAt  The timestamp of creation.
     */
    public Message(int id, int senderId, String senderName, String subject, String content, Timestamp createdAt) {
        this.id = id;
        this.senderId = senderId;
        this.senderName = senderName;
        this.subject = subject;
        this.content = content;
        this.createdAt = createdAt;
        this.isRead = false;
    }

    // Getters and Setters

    /**
     * Retrieves the unique message ID.
     *
     * @return The message ID.
     */
    public int getId() { return id; }

    /**
     * Retrieves the ID of the sender.
     *
     * @return The sender's user ID.
     */
    public int getSenderId() { return senderId; }

    /**
     * Retrieves the name of the sender.
     *
     * @return The sender's name.
     */
    public String getSenderName() { return senderName; }

    /**
     * Retrieves the subject line of the message.
     *
     * @return The subject string.
     */
    public String getSubject() { return subject; }

    /**
     * Retrieves the body content of the message.
     *
     * @return The content string.
     */
    public String getContent() { return content; }

    /**
     * Retrieves the timestamp when the message was created.
     *
     * @return The creation {@link Timestamp}.
     */
    public Timestamp getCreatedAt() { return createdAt; }
    
    /**
     * Returns a formatted string representation of the message.
     * <p>
     * The format is: {@code [YYYY-MM-DD HH:MM] SenderName: Subject}
     * </p>
     *
     * @return A string suitable for displaying in message lists or logs.
     */
    @Override
    public String toString() {
        return String.format("[%s] %s: %s", createdAt.toString().substring(0,16), senderName, subject);
    }
}