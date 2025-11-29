package com.authflow.repository;

import com.authflow.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for User entity operations.
 * 
 * <h2>Interview Q&A:</h2>
 * <p>
 * <b>Q:</b> What is the difference between findById and findByUsername?
 * </p>
 * <p>
 * <b>A:</b> findById is inherited from JpaRepository and uses the primary key.
 * findByUsername is a custom query method that Spring Data JPA automatically
 * implements
 * based on the method name (Query Derivation).
 * </p>
 * 
 * @author Shivam Srivastav
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
