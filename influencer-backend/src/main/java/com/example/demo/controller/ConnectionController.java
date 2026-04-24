package com.example.demo.controller;

import com.example.demo.dto.ConnectionRequestDto;
import com.example.demo.model.ConnectionRequest;
import com.example.demo.security.JwtUtil;
import com.example.demo.service.ConnectionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * MVC Controller — handles HTTP concerns for collaboration request resources.
 * JWT extraction happens here (HTTP concern); ownership checks stay in this layer
 * as they relate to HTTP identity. All persistence logic is in {@link ConnectionService}.
 */
@RestController
@CrossOrigin
@RequestMapping("/connections")
public class ConnectionController {

    private final ConnectionService connectionService;

    public ConnectionController(ConnectionService connectionService) {
        this.connectionService = connectionService;
    }

    /**
     * POST /connections/request
     * Sends a collaboration request. The business name is derived from the JWT — not the request body.
     */
    @PostMapping("/request")
    public ResponseEntity<String> sendRequest(
            @RequestBody ConnectionRequestDto dto,
            HttpServletRequest http) {

        String business = extractUsername(http);
        if (business == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token");
        }
        if (dto.getInfluencerUsername() == null || dto.getInfluencerUsername().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Influencer username is required");
        }

        ConnectionRequest entity = new ConnectionRequest();
        entity.setBusinessName(business);
        entity.setInfluencerUsername(dto.getInfluencerUsername());

        return ResponseEntity.ok(connectionService.sendRequest(entity));
    }

    /**
     * GET /connections/all
     * Returns all connection requests (admin-level view).
     */
    @GetMapping("/all")
    public ResponseEntity<List<ConnectionRequest>> getAllRequests() {
        return ResponseEntity.ok(connectionService.getAllRequests());
    }

    /**
     * GET /connections/influencer
     * Returns all requests addressed to the authenticated influencer.
     */
    @GetMapping("/influencer")
    public ResponseEntity<List<ConnectionRequest>> myInfluencerRequests(HttpServletRequest http) {
        String influencer = extractUsername(http);
        if (influencer == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(connectionService.getRequestsForInfluencer(influencer));
    }

    /**
     * GET /connections/business
     * Returns all requests sent by the authenticated business.
     */
    @GetMapping("/business")
    public ResponseEntity<List<ConnectionRequest>> myBusinessRequests(HttpServletRequest http) {
        String business = extractUsername(http);
        if (business == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(connectionService.getRequestsForBusiness(business));
    }

    /**
     * PUT /connections/{id}/accept
     * Accepts a connection request. Only the target influencer may accept.
     */
    @PutMapping("/{id}/accept")
    public ResponseEntity<String> acceptRequest(@PathVariable Long id, HttpServletRequest http) {
        return updateRequestStatus(id, http, "ACCEPTED");
    }

    /**
     * PUT /connections/{id}/reject
     * Rejects a connection request. Only the target influencer may reject.
     */
    @PutMapping("/{id}/reject")
    public ResponseEntity<String> rejectRequest(@PathVariable Long id, HttpServletRequest http) {
        return updateRequestStatus(id, http, "REJECTED");
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private ResponseEntity<String> updateRequestStatus(Long id, HttpServletRequest http, String action) {
        String influencer = extractUsername(http);
        if (influencer == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token");
        }

        Optional<ConnectionRequest> opt = connectionService.findById(id);
        if (opt.isPresent()) {
            String target = opt.get().getInfluencerUsername();
            if (target != null && !target.equalsIgnoreCase(influencer)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not allowed");
            }
        }

        String result = "ACCEPTED".equals(action)
                ? connectionService.acceptRequest(id)
                : connectionService.rejectRequest(id);

        return ResponseEntity.ok(result);
    }

    /**
     * Extracts the username claim from the Bearer JWT in the Authorization header.
     *
     * @return username string, or null if the header is absent or the token is invalid.
     */
    private String extractUsername(HttpServletRequest http) {
        String header = http.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) return null;
        try {
            return JwtUtil.extractUsername(header.substring(7).trim());
        } catch (Exception e) {
            return null;
        }
    }
}
