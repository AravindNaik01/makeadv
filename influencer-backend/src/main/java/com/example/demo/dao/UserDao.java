package com.example.demo.dao;

import com.example.demo.model.User;

/**
 * DAO contract for User persistence operations.
 * Controllers and services depend on this interface, not on JPA details.
 */
public interface UserDao {

    /** Persist a new or updated User. */
    User save(User user);

    /** Find a user by their unique username. Returns null if not found. */
    User findByUsername(String username);
}
