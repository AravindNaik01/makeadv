package com.example.demo.repository;

import com.example.demo.dao.InfluencerDao;
import com.example.demo.model.Influencer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA implementation of {@link InfluencerDao}.
 */
@Repository
public interface InfluencerRepository extends JpaRepository<Influencer, Long>, InfluencerDao {

    @Override
    List<Influencer> findByLocationIgnoreCaseAndCategoryIgnoreCase(String location, String category);

    @Override
    Optional<Influencer> findByUsernameIgnoreCase(String username);
}
