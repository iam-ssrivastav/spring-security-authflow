package com.authflow.service;

import com.authflow.model.User;
import com.authflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Custom UserDetailsService implementation for Spring Security.
 * 
 * <h2>Interview Q&A:</h2>
 * 
 * <p>
 * <b>Q:</b> What is UserDetailsService and why is it needed?
 * </p>
 * <p>
 * <b>A:</b> UserDetailsService is a core interface in Spring Security used to
 * retrieve
 * user-related data. It has one method: loadUserByUsername(). Spring Security
 * uses this
 * to load user details during authentication.
 * </p>
 * 
 * <p>
 * <b>Q:</b> What is the difference between User entity and UserDetails?
 * </p>
 * <p>
 * <b>A:</b>
 * <ul>
 * <li>User entity: JPA entity representing user in database</li>
 * <li>UserDetails: Spring Security interface representing authenticated
 * user</li>
 * <li>UserDetailsService bridges the gap between them</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <b>Q:</b> Why use @Transactional on loadUserByUsername?
 * </p>
 * <p>
 * <b>A:</b> To ensure lazy-loaded associations (like roles and permissions) are
 * fetched within the transaction boundary, preventing
 * LazyInitializationException.
 * </p>
 * 
 * @author Shivam Srivastav
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

        private final UserRepository userRepository;

        /**
         * Load user by username for authentication.
         * Cached to avoid repeated database queries.
         * 
         * @param username Username to load
         * @return UserDetails object
         * @throws UsernameNotFoundException if user not found
         */
        @Override
        @Transactional(readOnly = true)
        @Cacheable(value = "users", key = "#username")
        public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                User user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

                return org.springframework.security.core.userdetails.User.builder()
                                .username(user.getUsername())
                                .password(user.getPassword())
                                .authorities(getAuthorities(user))
                                .accountExpired(!user.isAccountNonExpired())
                                .accountLocked(!user.isAccountNonLocked())
                                .credentialsExpired(!user.isCredentialsNonExpired())
                                .disabled(!user.isEnabled())
                                .build();
        }

        /**
         * Convert user roles and permissions to Spring Security authorities.
         * 
         * <p>
         * <b>Interview Tip:</b> Authorities in Spring Security are strings that
         * represent
         * permissions. They can be roles (prefixed with ROLE_) or specific permissions.
         * </p>
         */
        private Collection<? extends GrantedAuthority> getAuthorities(User user) {
                // Add role-based authorities
                var roleAuthorities = user.getRoles().stream()
                                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                                .collect(Collectors.toSet());

                // Add permission-based authorities
                var permissionAuthorities = user.getRoles().stream()
                                .flatMap(role -> role.getPermissions().stream())
                                .map(permission -> new SimpleGrantedAuthority(permission.getName().name()))
                                .collect(Collectors.toSet());

                roleAuthorities.addAll(permissionAuthorities);
                return roleAuthorities;
        }
}
