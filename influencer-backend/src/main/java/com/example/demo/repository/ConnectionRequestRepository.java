package com.example.demo.repository;

import com.example.demo.dao.ConnectionRequestDao;
import com.example.demo.model.ConnectionRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA implementation of {@link ConnectionRequestDao}.
 */
@Repository
public interface ConnectionRequestRepository extends JpaRepository<ConnectionRequest, Long>, ConnectionRequestDao {

    @Override
    List<ConnectionRequest> findByInfluencerUsernameIgnoreCaseOrderByIdDesc(String influencerUsername);

    @Override
    List<ConnectionRequest> findByBusinessNameOrderByIdDesc(String businessName);
}
