package com.example.demo.dao;

import com.example.demo.model.ConnectionRequest;
import java.util.List;
import java.util.Optional;

/**
 * DAO contract for ConnectionRequest persistence operations.
 */
public interface ConnectionRequestDao {

    /** Persist a new or updated ConnectionRequest. */
    ConnectionRequest save(ConnectionRequest request);

    /** Return all ConnectionRequests. */
    List<ConnectionRequest> findAll();

    /** Find a single ConnectionRequest by its primary key. */
    Optional<ConnectionRequest> findById(Long id);

    /** Return all requests sent to a specific influencer, newest first. */
    List<ConnectionRequest> findByInfluencerUsernameIgnoreCaseOrderByIdDesc(String influencerUsername);

    /** Return all requests sent by a specific business, newest first. */
    List<ConnectionRequest> findByBusinessNameOrderByIdDesc(String businessName);
}
