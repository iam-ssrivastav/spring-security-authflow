package com.authflow.config;

import com.authflow.security.algorithms.PasswordHashingAlgorithms;
import com.authflow.repository.PermissionRepository;
import com.authflow.repository.RoleRepository;
import com.authflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Data initialization for demo purposes.
 * 
 * NOTE: Data initialization is now handled by Flyway (V1__init.sql).
 * This class is kept for reference but disabled.
 * 
 * @author Shivam Srivastav
 */
@Component
@RequiredArgsConstructor
@Profile("!test") // Don't run in tests
public class DataInitializer implements CommandLineRunner {

        private final UserRepository userRepository;
        private final RoleRepository roleRepository;
        private final PermissionRepository permissionRepository;
        private final PasswordHashingAlgorithms passwordHashingAlgorithms;

        @Override
        public void run(String... args) throws Exception {
                // Data initialization is now handled by Flyway (V1__init.sql)
                // Keeping this class for reference or future non-SQL initialization
        }
}
