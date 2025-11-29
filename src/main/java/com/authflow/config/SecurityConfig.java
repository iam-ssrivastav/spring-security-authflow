package com.authflow.config;

import com.authflow.security.filters.JwtAuthenticationFilter;
import com.authflow.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security Configuration.
 * 
 * <h2>Security Architecture:</h2>
 * 
 * <pre>
 * Request → SecurityFilterChain → Filters → Controller
 *           ↓
 *           AuthenticationManager → AuthenticationProvider → UserDetailsService
 *                                   ↓
 *                                   PasswordEncoder
 * </pre>
 * 
 * <h2>Interview Q&A:</h2>
 * 
 * <p>
 * <b>Q:</b> What is SecurityFilterChain and how does it work?
 * </p>
 * <p>
 * <b>A:</b> SecurityFilterChain is a chain of filters that process HTTP
 * requests.
 * Each filter performs a specific security task (CSRF, authentication,
 * authorization, etc.).
 * Filters are executed in order, and each can modify the request/response or
 * stop the chain.
 * </p>
 * 
 * <p>
 * <b>Q:</b> What is the difference between @EnableWebSecurity
 * and @EnableMethodSecurity?
 * </p>
 * <p>
 * <b>A:</b>
 * <ul>
 * <li>@EnableWebSecurity: Enables Spring Security web support and provides
 * configuration</li>
 * <li>@EnableMethodSecurity: Enables method-level security annotations
 * like @PreAuthorize, @PostAuthorize</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <b>Q:</b> What are the different session management strategies?
 * </p>
 * <p>
 * <b>A:</b>
 * <ul>
 * <li>STATELESS: No session created (for JWT/token-based auth)</li>
 * <li>ALWAYS: Always create session</li>
 * <li>IF_REQUIRED: Create session if needed (default)</li>
 * <li>NEVER: Never create session, but use existing if present</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <b>Q:</b> How do you protect against CSRF attacks?
 * </p>
 * <p>
 * <b>A:</b> CSRF (Cross-Site Request Forgery) protection:
 * <ul>
 * <li>For session-based auth: Enable CSRF tokens (default in Spring
 * Security)</li>
 * <li>For token-based auth (JWT): Can disable CSRF if tokens are in headers
 * (not cookies)</li>
 * <li>Use SameSite cookie attribute</li>
 * <li>Validate Origin/Referer headers</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <b>Q:</b> What is the order of security filters?
 * </p>
 * <p>
 * <b>A:</b> Common filter order:
 * <ol>
 * <li>SecurityContextPersistenceFilter</li>
 * <li>LogoutFilter</li>
 * <li>UsernamePasswordAuthenticationFilter</li>
 * <li>Custom filters (like JwtAuthenticationFilter)</li>
 * <li>ExceptionTranslationFilter</li>
 * <li>FilterSecurityInterceptor</li>
 * </ol>
 * </p>
 * 
 * @author Shivam Srivastav
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Configure HTTP security.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF for stateless API
                .csrf(AbstractHttpConfigurer::disable)

                // Configure authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/public/**",
                                "/h2-console/**",
                                "/error",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**")
                        .permitAll()

                        // Admin endpoints
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // All other requests require authentication
                        .anyRequest().authenticated())

                // Stateless session management for JWT
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Add JWT filter before UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // Configure authentication provider
                .authenticationProvider(authenticationProvider())

                // Allow H2 console frames
                .headers(headers -> headers.frameOptions().disable());

        return http.build();
    }

    /**
     * Authentication provider using UserDetailsService and PasswordEncoder.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Password encoder using BCrypt.
     * 
     * <p>
     * <b>Interview Tip:</b> BCrypt automatically handles salt generation and
     * storage.
     * The work factor (strength) is 10 by default, can be increased for more
     * security.
     * </p>
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * Authentication manager bean.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
