package com.authflow.repository;

import com.authflow.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Permission entity operations.
 * 
 * @author Shivam Srivastav
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    Optional<Permission> findByName(Permission.PermissionName name);
}
