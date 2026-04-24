package com.example.demo.dao;

import com.example.demo.model.Business;
import java.util.List;
import java.util.Optional;

/**
 * DAO contract for Business persistence operations.
 */
public interface BusinessDao {

    /** Persist a new or updated Business. */
    Business save(Business business);

    /** Return all Business records. */
    List<Business> findAll();

    /** Find a business by the owner's username. */
    Optional<Business> findByUsername(String username);
}
