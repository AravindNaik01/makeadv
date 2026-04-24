package com.example.demo.repository;

import com.example.demo.dao.UserDao;
import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA implementation of {@link UserDao}.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long>, UserDao {

    @Override
    User findByUsername(String username);
}
