package com.authflow.config;

import com.authflow.model.Permission;
import com.authflow.model.Role;
import com.authflow.model.User;
import com.authflow.repository.PermissionRepository;
import com.authflow.repository.RoleRepository;
import com.authflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Data initialization for demo purposes.
 * Creates default roles, permissions, and users.
 * 
 * @author Shivam Srivastav
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Create permissions
        Permission readDoc = createPermission(Permission.PermissionName.READ_DOCUMENT, "Read documents");
        Permission writeDoc = createPermission(Permission.PermissionName.WRITE_DOCUMENT, "Write documents");
        Permission deleteDoc = createPermission(Permission.PermissionName.DELETE_DOCUMENT, "Delete documents");
        Permission readUser = createPermission(Permission.PermissionName.READ_USER, "Read users");
        Permission createUser = createPermission(Permission.PermissionName.CREATE_USER, "Create users");
        Permission updateUser = createPermission(Permission.PermissionName.UPDATE_USER, "Update users");
        Permission deleteUser = createPermission(Permission.PermissionName.DELETE_USER, "Delete users");

        // Create roles with permissions
        Role guestRole = createRole(Role.RoleName.ROLE_GUEST, "Guest user", 1, Set.of(readDoc));
        Role userRole = createRole(Role.RoleName.ROLE_USER, "Standard user", 2, Set.of(readDoc, writeDoc, readUser));
        Role managerRole = createRole(Role.RoleName.ROLE_MANAGER, "Manager", 3,
                Set.of(readDoc, writeDoc, deleteDoc, readUser, createUser, updateUser));
        Role adminRole = createRole(Role.RoleName.ROLE_ADMIN, "Administrator", 4,
                Set.of(readDoc, writeDoc, deleteDoc, readUser, createUser, updateUser, deleteUser));

        // Create demo users
        createUser("user", "user@example.com", "password123", Set.of(userRole));
        createUser("manager", "manager@example.com", "password123", Set.of(managerRole));
        createUser("admin", "admin@example.com", "password123", Set.of(adminRole));

        System.out.println("\n=== Demo Users Created ===");
        System.out.println("Username: user | Password: password123 | Role: USER");
        System.out.println("Username: manager | Password: password123 | Role: MANAGER");
        System.out.println("Username: admin | Password: password123 | Role: ADMIN");
        System.out.println("==========================\n");
    }

    private Permission createPermission(Permission.PermissionName name, String description) {
        return permissionRepository.findByName(name)
                .orElseGet(() -> permissionRepository.save(
                        Permission.builder()
                                .name(name)
                                .description(description)
                                .build()));
    }

    private Role createRole(Role.RoleName name, String description, int level, Set<Permission> permissions) {
        return roleRepository.findByName(name)
                .orElseGet(() -> roleRepository.save(
                        Role.builder()
                                .name(name)
                                .description(description)
                                .hierarchyLevel(level)
                                .permissions(permissions)
                                .build()));
    }

    private void createUser(String username, String email, String password, Set<Role> roles) {
        if (!userRepository.existsByUsername(username)) {
            userRepository.save(
                    User.builder()
                            .username(username)
                            .email(email)
                            .password(passwordEncoder.encode(password))
                            .roles(roles)
                            .enabled(true)
                            .accountNonExpired(true)
                            .accountNonLocked(true)
                            .credentialsNonExpired(true)
                            .mfaEnabled(false)
                            .build());
        }
    }
}
