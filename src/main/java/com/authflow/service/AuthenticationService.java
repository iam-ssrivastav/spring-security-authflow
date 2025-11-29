package com.authflow.service;

import com.authflow.auth.jwt.JwtTokenService;
import com.authflow.dto.AuthResponse;
import com.authflow.dto.LoginRequest;
import com.authflow.dto.RegisterRequest;
import com.authflow.model.RefreshToken;
import com.authflow.model.Role;
import com.authflow.model.User;
import com.authflow.repository.RefreshTokenRepository;
import com.authflow.repository.RoleRepository;
import com.authflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Authentication Service handling user registration and login.
 * 
 * <h2>Interview Q&A:</h2>
 * 
 * <p>
 * <b>Q:</b> What is AuthenticationManager and how does it work?
 * </p>
 * <p>
 * <b>A:</b> AuthenticationManager is the main strategy interface for
 * authentication.
 * It has one method: authenticate(Authentication). It delegates to
 * AuthenticationProvider
 * implementations to perform actual authentication. DaoAuthenticationProvider
 * is commonly
 * used for username/password authentication.
 * </p>
 * 
 * <p>
 * <b>Q:</b> What happens during the authentication process?
 * </p>
 * <p>
 * <b>A:</b> Flow:
 * <ol>
 * <li>Create UsernamePasswordAuthenticationToken with credentials</li>
 * <li>AuthenticationManager delegates to AuthenticationProvider</li>
 * <li>Provider uses UserDetailsService to load user</li>
 * <li>Provider uses PasswordEncoder to verify password</li>
 * <li>If successful, returns fully authenticated Authentication object</li>
 * <li>SecurityContext is updated with authentication</li>
 * </ol>
 * </p>
 * 
 * <p>
 * <b>Q:</b> Why use @Transactional for registration?
 * </p>
 * <p>
 * <b>A:</b> To ensure atomicity - if any step fails (user save, role
 * assignment),
 * the entire operation is rolled back, maintaining database consistency.
 * </p>
 * 
 * @author Shivam Srivastav
 */
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    /**
     * Register a new user.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if user already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Create new user
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .mfaEnabled(false)
                .build();

        // Assign default role
        Role userRole = roleRepository.findByName(Role.RoleName.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Default role not found"));
        user.setRoles(Set.of(userRole));

        // Save user
        user = userRepository.save(user);

        // Send welcome email
        emailService.sendWelcomeEmail(user.getEmail(), user.getUsername());

        // Generate tokens
        String accessToken = jwtTokenService.generateAccessToken(user);
        String refreshToken = createRefreshToken(user);

        return buildAuthResponse(user, accessToken, refreshToken);
    }

    /**
     * Authenticate user and generate tokens.
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()));

        // Load user
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update last login
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        // Check MFA
        if (user.isMfaEnabled()) {
            // MFA verification required
            return AuthResponse.builder()
                    .mfaRequired(true)
                    .username(user.getUsername())
                    .build();
        }

        // Generate tokens
        String accessToken = jwtTokenService.generateAccessToken(user);
        String refreshToken = createRefreshToken(user);

        return buildAuthResponse(user, accessToken, refreshToken);
    }

    /**
     * Refresh access token using refresh token.
     */
    @Transactional
    public AuthResponse refreshToken(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (refreshToken.isExpired() || refreshToken.isRevoked()) {
            throw new RuntimeException("Refresh token is expired or revoked");
        }

        User user = refreshToken.getUser();
        String newAccessToken = jwtTokenService.generateAccessToken(user);

        return buildAuthResponse(user, newAccessToken, refreshTokenValue);
    }

    /**
     * Create and store refresh token.
     */
    private String createRefreshToken(User user) {
        String tokenValue = UUID.randomUUID().toString();

        RefreshToken refreshToken = RefreshToken.builder()
                .token(tokenValue)
                .user(user)
                .expiryDate(LocalDateTime.now().plusSeconds(refreshExpiration / 1000))
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);
        return tokenValue;
    }

    /**
     * Build authentication response.
     */
    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        String[] roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .toArray(String[]::new);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtExpiration / 1000)
                .username(user.getUsername())
                .roles(roles)
                .mfaRequired(false)
                .build();
    }
}
