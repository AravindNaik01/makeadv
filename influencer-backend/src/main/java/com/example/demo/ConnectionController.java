package com.example.demo;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@CrossOrigin
public class ConnectionController {

    private final ConnectionService service;

    public ConnectionController(ConnectionService service) {
        this.service = service;
    }

    @PostMapping("/request")
    public ResponseEntity<String> sendRequest(@RequestBody ConnectionRequest request, HttpServletRequest http) {
        String business = usernameFromAuth(http);
        if (business == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing token");
        }
        if (request == null || request.getInfluencerUsername() == null || request.getInfluencerUsername().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Influencer username is required");
        }
        request.setBusinessName(business);
        String msg = service.sendRequest(request);
        return ResponseEntity.ok(msg);
    }

    @GetMapping("/requests")
    public List<ConnectionRequest> getAllRequests() {
        return service.getAllRequests();
    }

    @GetMapping("/requests/influencer")
    public List<ConnectionRequest> myInfluencerRequests(HttpServletRequest http) {
        String influencer = usernameFromAuth(http);
        if (influencer == null) return List.of();
        return service.getRequestsForInfluencer(influencer);
    }

    @GetMapping("/requests/business")
    public List<ConnectionRequest> myBusinessRequests(HttpServletRequest http) {
        String business = usernameFromAuth(http);
        if (business == null) return List.of();
        return service.getRequestsForBusiness(business);
    }

    @PutMapping("/accept/{id}")
    public ResponseEntity<String> acceptRequest(@PathVariable("id") Long id, HttpServletRequest http) {
        String influencer = usernameFromAuth(http);
        if (influencer == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing token");
        }
        ConnectionRequest req = service.getAllRequests().stream().filter(r -> r.getId().equals(id)).findFirst().orElse(null);
        if (req != null && req.getInfluencerUsername() != null && !req.getInfluencerUsername().equalsIgnoreCase(influencer)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not allowed");
        }
        return ResponseEntity.ok(service.acceptRequest(id));
    }

    @PutMapping("/reject/{id}")
    public ResponseEntity<String> rejectRequest(@PathVariable("id") Long id, HttpServletRequest http) {
        String influencer = usernameFromAuth(http);
        if (influencer == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing token");
        }
        ConnectionRequest req = service.getAllRequests().stream().filter(r -> r.getId().equals(id)).findFirst().orElse(null);
        if (req != null && req.getInfluencerUsername() != null && !req.getInfluencerUsername().equalsIgnoreCase(influencer)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not allowed");
        }
        return ResponseEntity.ok(service.rejectRequest(id));
    }

    private String usernameFromAuth(HttpServletRequest http) {
        String header = http.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) return null;
        String token = header.substring(7);
        try {
            return JwtUtil.extractUsername(token);
        } catch (Exception e) {
            return null;
        }
    }
}