package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * JPA Entity representing a chat message exchanged between a Business and an Influencer.
 * Messages are scoped to a connection (identified by connectionId).
 */
@Entity
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The connection request this message belongs to */
    @Column(nullable = false)
    private Long connectionId;

    /** Username of the sender (business or influencer) */
    @Column(nullable = false)
    private String sender;

    /** Username of the recipient */
    @Column(nullable = false)
    private String recipient;

    /** The message content */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /** TEXT | IMAGE | VIDEO | DOCUMENT */
    @Column(nullable = false, length = 20)
    private String messageType = "TEXT";

    @Column
    private String fileName;

    @Column(columnDefinition = "TEXT")
    private String fileUrl;

    @Column
    private Long fileSize;

    @Column
    private String mimeType;

    /** Timestamp when the message was sent */
    @Column(nullable = false)
    private LocalDateTime sentAt;

    @PrePersist
    protected void onCreate() {
        sentAt = LocalDateTime.now();
    }

    // ── Getters & Setters ──────────────────────────────────────────────────────

    public Long getId() { return id; }

    public Long getConnectionId() { return connectionId; }
    public void setConnectionId(Long connectionId) { this.connectionId = connectionId; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
}
