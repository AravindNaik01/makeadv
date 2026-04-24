package com.example.demo.repository;

import com.example.demo.dao.BusinessDao;
import com.example.demo.model.Business;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA implementation of {@link BusinessDao}.
 */
@Repository
public interface BusinessRepository extends JpaRepository<Business, Long>, BusinessDao {

    @Override
    Optional<Business> findByUsername(String username);
}
