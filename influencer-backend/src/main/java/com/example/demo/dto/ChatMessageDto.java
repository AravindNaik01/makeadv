package com.example.demo.dto;

/**
 * DTO for incoming chat message payloads sent over STOMP WebSocket.
 */
public class ChatMessageDto {

    private Long connectionId;
    private String sender;
    private String recipient;
    private String content;
    private String messageType; // TEXT | IMAGE | VIDEO | DOCUMENT
    private String fileName;
    private String fileUrl;
    private Long fileSize;
    private String mimeType;

    // ── Getters & Setters ──────────────────────────────────────────────────────

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
}
