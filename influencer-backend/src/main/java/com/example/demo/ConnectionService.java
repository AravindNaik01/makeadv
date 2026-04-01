package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConnectionService {

    @Autowired
    private ConnectionRepository repo;

    public String sendRequest(ConnectionRequest request) {
        String u = request.getInfluencerUsername();
        if (u != null) {
            request.setInfluencerUsername(u.trim());
        }
        request.setStatus("PENDING");
        repo.save(request);
        return "Request sent!";
    }

    public List<ConnectionRequest> getAllRequests() {
        return repo.findAll();
    }

    public List<ConnectionRequest> getRequestsForInfluencer(String influencerUsername) {
        if (influencerUsername == null || influencerUsername.isBlank()) {
            return List.of();
        }
        return repo.findByInfluencerUsernameIgnoreCaseOrderByIdDesc(influencerUsername.trim());
    }

    public List<ConnectionRequest> getRequestsForBusiness(String businessName) {
        return repo.findByBusinessNameOrderByIdDesc(businessName);
    }

    public String acceptRequest(Long id) {
        ConnectionRequest req = repo.findById(id).orElse(null);

        if (req == null) {
            return "Request not found";
        }

        req.setStatus("ACCEPTED");
        repo.save(req);

        return "Request accepted!";
    }

    public String rejectRequest(Long id) {
        ConnectionRequest req = repo.findById(id).orElse(null);

        if (req == null) {
            return "Request not found";
        }

        req.setStatus("REJECTED");
        repo.save(req);

        return "Request rejected!";
    }
}