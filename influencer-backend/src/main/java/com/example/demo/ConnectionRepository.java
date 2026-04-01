package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConnectionRepository extends JpaRepository<ConnectionRequest, Long> {
    List<ConnectionRequest> findByInfluencerUsernameOrderByIdDesc(String influencerUsername);

    List<ConnectionRequest> findByInfluencerUsernameIgnoreCaseOrderByIdDesc(String influencerUsername);

    List<ConnectionRequest> findByBusinessNameOrderByIdDesc(String businessName);
}