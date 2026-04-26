package com.example.demo.websocket;

import com.example.demo.dto.ChatMessageDto;
import com.example.demo.model.ConnectionRequest;
import com.example.demo.model.ChatMessage;
import com.example.demo.repository.ChatMessageRepository;
import com.example.demo.repository.ConnectionRequestRepository;
import com.example.demo.security.JwtUtil;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpHeaders;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Handles real-time chat via STOMP WebSocket and provides REST endpoints
 * for fetching message history.
 */
@Controller
public class ChatController {
    private static final Path UPLOAD_DIR = Paths.get("uploads", "chat");
    private static final long MAX_UPLOAD_BYTES = 25L * 1024 * 1024; // 25 MB
    private static final Set<String> ALLOWED_MIME_PREFIXES = Set.of("image/", "video/");
    private static final Set<String> ALLOWED_DOC_MIME_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "text/plain"
    );

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageRepository chatMessageRepository;
    private final ConnectionRequestRepository connectionRequestRepository;

    public ChatController(SimpMessagingTemplate messagingTemplate,
                          ChatMessageRepository chatMessageRepository,
                          ConnectionRequestRepository connectionRequestRepository) {
        this.messagingTemplate = messagingTemplate;
        this.chatMessageRepository = chatMessageRepository;
        this.connectionRequestRepository = connectionRequestRepository;
    }

    /**
     * STOMP endpoint: /app/chat.send
     * Persists the message and broadcasts it to both participants via their private queues.
     */
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessageDto dto) {
        // Persist the message
        ChatMessage msg = new ChatMessage();
        msg.setConnectionId(dto.getConnectionId());
        msg.setSender(dto.getSender());
        msg.setRecipient(dto.getRecipient());
        msg.setContent(dto.getContent() == null ? "" : dto.getContent());
        msg.setMessageType(dto.getMessageType() == null ? "TEXT" : dto.getMessageType());
        msg.setFileName(dto.getFileName());
        msg.setFileUrl(dto.getFileUrl());
        msg.setFileSize(dto.getFileSize());
        msg.setMimeType(dto.getMimeType());
        ChatMessage saved = chatMessageRepository.save(msg);

        // Build the response DTO from the saved entity (includes generated id + sentAt)
        ChatMessageDto response = new ChatMessageDto();
        response.setConnectionId(saved.getConnectionId());
        response.setSender(saved.getSender());
        response.setRecipient(saved.getRecipient());
        response.setContent(saved.getContent());
        response.setMessageType(saved.getMessageType());
        response.setFileName(saved.getFileName());
        response.setFileUrl(saved.getFileUrl());
        response.setFileSize(saved.getFileSize());
        response.setMimeType(saved.getMimeType());

        // Deliver to both the sender and recipient via their private queues
        String topic = "/topic/chat/" + saved.getConnectionId();
        messagingTemplate.convertAndSend(topic, response);
    }

    @PostMapping("/chat/upload")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "File is required"));
        }
        if (file.getSize() > MAX_UPLOAD_BYTES) {
            return ResponseEntity.badRequest().body(Map.of("message", "File too large. Max allowed is 25 MB"));
        }
        try {
            Files.createDirectories(UPLOAD_DIR);

            String original = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
            String safeName = original.replaceAll("[^a-zA-Z0-9._-]", "_");
            String storedName = UUID.randomUUID() + "_" + safeName;

            Path target = UPLOAD_DIR.resolve(storedName).normalize();
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            String mime = file.getContentType() == null ? "application/octet-stream" : file.getContentType();
            if (!isAllowedMimeType(mime)) {
                Files.deleteIfExists(target);
                return ResponseEntity.badRequest().body(Map.of("message", "Unsupported file type"));
            }
            String messageType = detectMessageType(mime);

            return ResponseEntity.ok(Map.of(
                    "fileName", original,
                    "fileUrl", "/chat/files/" + storedName,
                    "fileSize", file.getSize(),
                    "mimeType", mime,
                    "messageType", messageType
            ));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to upload file"));
        }
    }

    @GetMapping("/chat/files/{fileName:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String fileName,
                                              @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
                                              @RequestParam(value = "token", required = false) String tokenParam) {
        try {
            String username = extractUsername(authHeader, tokenParam);
            if (username == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid token");
            }

            Path root = UPLOAD_DIR.toAbsolutePath().normalize();
            Path filePath = root.resolve(fileName).normalize();
            if (!filePath.startsWith(root) || !Files.exists(filePath)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found");
            }

            String fileUrl = "/chat/files/" + fileName;
            ChatMessage message = chatMessageRepository.findTopByFileUrlOrderByIdDesc(fileUrl)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "File metadata not found"));

            ConnectionRequest req = connectionRequestRepository.findAll().stream()
                    .filter(r -> r.getId() != null && r.getId().equals(message.getConnectionId()))
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized"));
            boolean allowed = username.equalsIgnoreCase(req.getBusinessName()) ||
                    username.equalsIgnoreCase(req.getInfluencerUsername());
            if (!allowed) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized");
            }

            Resource resource = new UrlResource(filePath.toUri());
            String mime = Files.probeContentType(filePath);
            MediaType mediaType = mime == null ? MediaType.APPLICATION_OCTET_STREAM : MediaType.parseMediaType(mime);
            return ResponseEntity.ok().contentType(mediaType).body(resource);
        } catch (MalformedURLException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file path");
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Cannot read file");
        }
    }

    private String detectMessageType(String mimeType) {
        if (mimeType.startsWith("image/")) return "IMAGE";
        if (mimeType.startsWith("video/")) return "VIDEO";
        return "DOCUMENT";
    }

    private boolean isAllowedMimeType(String mimeType) {
        if (mimeType == null || mimeType.isBlank()) return false;
        for (String prefix : ALLOWED_MIME_PREFIXES) {
            if (mimeType.startsWith(prefix)) return true;
        }
        return ALLOWED_DOC_MIME_TYPES.contains(mimeType);
    }

    private String extractUsername(String authHeader, String tokenParam) {
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                return JwtUtil.extractUsername(authHeader.substring(7).trim());
            }
            if (tokenParam != null && !tokenParam.isBlank()) {
                return JwtUtil.extractUsername(tokenParam.trim());
            }
            return null;
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * REST GET /chat/history/{connectionId}
     * Returns all persisted messages for the given connection.
     */
    @RestController
    @RequestMapping("/chat")
    @CrossOrigin
    public static class ChatHistoryController {

        private final ChatMessageRepository repo;

        public ChatHistoryController(ChatMessageRepository repo) {
            this.repo = repo;
        }

        @GetMapping("/history/{connectionId}")
        public List<ChatMessage> getHistory(@PathVariable Long connectionId) {
            return repo.findByConnectionIdOrderBySentAtAsc(connectionId);
        }
    }
}
