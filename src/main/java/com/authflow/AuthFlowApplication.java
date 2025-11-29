package com.authflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for AuthFlow - Complete Authentication & Authorization
 * Demo.
 * 
 * <p>
 * This application demonstrates comprehensive authentication and authorization
 * mechanisms
 * including Basic Auth, Session-based, JWT, OAuth 2.0, MFA, RBAC, ABAC, and
 * more.
 * </p>
 * 
 * <h2>Interview Topics Covered:</h2>
 * <ul>
 * <li>Authentication vs Authorization</li>
 * <li>Stateless vs Stateful authentication</li>
 * <li>Token-based authentication (JWT)</li>
 * <li>OAuth 2.0 and OpenID Connect</li>
 * <li>Multi-factor authentication (MFA)</li>
 * <li>Role-Based Access Control (RBAC)</li>
 * <li>Attribute-Based Access Control (ABAC)</li>
 * <li>Security algorithms (BCrypt, Argon2, HMAC, RSA)</li>
 * </ul>
 * 
 * @author Shivam Srivastav
 * @version 1.0
 * @since 2025-11-30
 */
@SpringBootApplication
public class AuthFlowApplication {

    /**
     * Main entry point for the Spring Boot application.
     * 
     * <p>
     * <b>Interview Q&A:</b>
     * </p>
     * <p>
     * <b>Q:</b> What does @SpringBootApplication annotation do?
     * </p>
     * <p>
     * <b>A:</b> It's a convenience annotation that combines:
     * <ul>
     * <li>@Configuration - Marks class as source of bean definitions</li>
     * <li>@EnableAutoConfiguration - Enables Spring Boot's auto-configuration</li>
     * <li>@ComponentScan - Enables component scanning in current package and
     * sub-packages</li>
     * </ul>
     * </p>
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(AuthFlowApplication.class, args);
    }
}
