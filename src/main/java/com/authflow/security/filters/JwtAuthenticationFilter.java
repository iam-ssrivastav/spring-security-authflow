package com.authflow.security.filters;

import com.authflow.auth.jwt.JwtTokenService;
import com.authflow.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter that intercepts requests and validates JWT tokens.
 * 
 * <h2>Filter Chain Execution:</h2>
 * 
 * <pre>
 * 1. Extract JWT from Authorization header
 * 2. Validate JWT signature and expiration
 * 3. Extract username from JWT
 * 4. Load user details from database
 * 5. Create authentication object
 * 6. Set authentication in SecurityContext
 * 7. Continue filter chain
 * </pre>
 * 
 * <h2>Interview Q&A:</h2>
 * 
 * <p>
 * <b>Q:</b> What is OncePerRequestFilter and why use it?
 * </p>
 * <p>
 * <b>A:</b> OncePerRequestFilter ensures the filter is executed only once per
 * request,
 * even if the request is forwarded or included. This prevents duplicate
 * processing.
 * </p>
 * 
 * <p>
 * <b>Q:</b> Where should JWT be sent in the request?
 * </p>
 * <p>
 * <b>A:</b> Best practices:
 * <ul>
 * <li>Authorization header: "Authorization: Bearer {token}" (most common)</li>
 * <li>Cookie: More secure with httpOnly flag</li>
 * <li>Avoid query parameters or request body (less secure)</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <b>Q:</b> What is SecurityContext and why is it important?
 * </p>
 * <p>
 * <b>A:</b> SecurityContext holds the authentication information for the
 * current thread.
 * It's stored in ThreadLocal, making it accessible throughout the request
 * processing without
 * passing it explicitly. Spring Security uses it for authorization decisions.
 * </p>
 * 
 * @author Shivam Srivastav
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            // Extract JWT from request
            String jwt = extractJwtFromRequest(request);

            if (jwt != null && jwtTokenService.validateToken(jwt)) {
                // Get username from token
                String username = jwtTokenService.getUsernameFromToken(jwt);

                // Load user details
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // Create authentication object
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities());

                // Set additional details
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));

                // Set authentication in SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e);
        }

        // Continue filter chain
        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token from Authorization header.
     * 
     * @param request HTTP request
     * @return JWT token or null
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }
}
