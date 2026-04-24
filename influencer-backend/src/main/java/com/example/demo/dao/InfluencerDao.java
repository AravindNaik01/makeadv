package com.example.demo.dao;

import com.example.demo.model.Influencer;
import java.util.List;
import java.util.Optional;

/**
 * DAO contract for Influencer persistence operations.
 */
public interface InfluencerDao {

    /** Persist a new or updated Influencer. */
    Influencer save(Influencer influencer);

    /** Return all Influencer records. */
    List<Influencer> findAll();

    /** Find influencers matching a specific location and category. */
    List<Influencer> findByLocationIgnoreCaseAndCategoryIgnoreCase(String location, String category);

    /** Find an influencer by username (case-insensitive). */
    Optional<Influencer> findByUsernameIgnoreCase(String username);
}
