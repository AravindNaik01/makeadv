package com.example.demo.service;

import com.example.demo.dao.ConnectionRequestDao;
import com.example.demo.model.ConnectionRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Business logic for managing collaboration requests between businesses and influencers.
 * Depends only on the {@link ConnectionRequestDao} interface.
 */
@Service
public class ConnectionService {

    private final ConnectionRequestDao connectionRequestDao;

    public ConnectionService(ConnectionRequestDao connectionRequestDao) {
        this.connectionRequestDao = connectionRequestDao;
    }

    /**
     * Saves a new connection request with status PENDING.
     * The businessName must already be set by the controller from the JWT principal.
     */
    public String sendRequest(ConnectionRequest request) {
        if (request.getInfluencerUsername() != null) {
            request.setInfluencerUsername(request.getInfluencerUsername().trim());
        }
        request.setStatus("PENDING");
        connectionRequestDao.save(request);
        return "Request sent!";
    }

    public List<ConnectionRequest> getAllRequests() {
        return connectionRequestDao.findAll();
    }

    public List<ConnectionRequest> getRequestsForInfluencer(String influencerUsername) {
        if (influencerUsername == null || influencerUsername.isBlank()) {
            return List.of();
        }
        return connectionRequestDao.findByInfluencerUsernameIgnoreCaseOrderByIdDesc(influencerUsername.trim());
    }

    public List<ConnectionRequest> getRequestsForBusiness(String businessName) {
        return connectionRequestDao.findByBusinessNameOrderByIdDesc(businessName);
    }

    /**
     * Transitions a request to ACCEPTED status.
     */
    public String acceptRequest(Long id) {
        return updateStatus(id, "ACCEPTED", "Request accepted!", "Request not found");
    }

    /**
     * Transitions a request to REJECTED status.
     */
    public String rejectRequest(Long id) {
        return updateStatus(id, "REJECTED", "Request rejected!", "Request not found");
    }

    /**
     * Finds a specific request — used by the controller for ownership checks.
     */
    public Optional<ConnectionRequest> findById(Long id) {
        return connectionRequestDao.findById(id);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private String updateStatus(Long id, String newStatus, String successMsg, String notFoundMsg) {
        Optional<ConnectionRequest> opt = connectionRequestDao.findById(id);
        if (opt.isEmpty()) {
            return notFoundMsg;
        }
        ConnectionRequest req = opt.get();
        req.setStatus(newStatus);
        connectionRequestDao.save(req);
        return successMsg;
    }
}
