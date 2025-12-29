package com.group12.greengrocer.models;

import java.sql.Timestamp;

public class Message {
    private int id;
    private int senderId;
    private String senderName; // JOIN ile kullanıcı adını alacağız
    private String subject;
    private String content;
    private Timestamp createdAt;
    private boolean isRead;

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
    public int getId() { return id; }
    public int getSenderId() { return senderId; }
    public String getSenderName() { return senderName; }
    public String getSubject() { return subject; }
    public String getContent() { return content; }
    public Timestamp getCreatedAt() { return createdAt; }
    
    @Override
    public String toString() {
        return String.format("[%s] %s: %s", createdAt.toString().substring(0,16), senderName, subject);
    }
}