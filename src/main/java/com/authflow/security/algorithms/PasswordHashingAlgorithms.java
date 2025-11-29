package com.authflow.security.algorithms;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Demonstrates different password hashing algorithms.
 * 
 * <h2>Password Hashing Algorithms Comparison:</h2>
 * 
 * <h3>1. BCrypt</h3>
 * <ul>
 * <li>Based on Blowfish cipher</li>
 * <li>Adaptive: Can increase work factor over time</li>
 * <li>Automatically handles salt generation</li>
 * <li>Default work factor: 10 (2^10 = 1024 iterations)</li>
 * <li>Best for: General purpose, widely adopted</li>
 * </ul>
 * 
 * <h3>2. Argon2</h3>
 * <ul>
 * <li>Winner of Password Hashing Competition (2015)</li>
 * <li>Memory-hard function (resistant to GPU/ASIC attacks)</li>
 * <li>Three variants: Argon2d, Argon2i, Argon2id (recommended)</li>
 * <li>Configurable: salt length, hash length, parallelism, memory,
 * iterations</li>
 * <li>Best for: Highest security requirements</li>
 * </ul>
 * 
 * <h3>3. PBKDF2</h3>
 * <ul>
 * <li>NIST recommended (FIPS compliant)</li>
 * <li>Uses HMAC with configurable hash function (SHA-256, SHA-512)</li>
 * <li>Configurable iterations (recommended: 310,000+ for SHA-256)</li>
 * <li>Best for: Compliance requirements, legacy systems</li>
 * </ul>
 * 
 * <h2>Interview Q&A:</h2>
 * 
 * <p>
 * <b>Q:</b> What is a salt and why is it important?
 * </p>
 * <p>
 * <b>A:</b> A salt is random data added to the password before hashing. It
 * prevents:
 * <ul>
 * <li>Rainbow table attacks (precomputed hash tables)</li>
 * <li>Identical passwords from having identical hashes</li>
 * <li>Parallel cracking of multiple passwords</li>
 * </ul>
 * Salt should be unique per password and stored alongside the hash.
 * </p>
 * 
 * <p>
 * <b>Q:</b> What is the work factor in BCrypt?
 * </p>
 * <p>
 * <b>A:</b> The work factor (cost factor) determines the number of iterations:
 * iterations = 2^workFactor. Higher work factor = more secure but slower.
 * As hardware improves, increase the work factor to maintain security.
 * </p>
 * 
 * <p>
 * <b>Q:</b> Why not use MD5 or SHA-1 for passwords?
 * </p>
 * <p>
 * <b>A:</b> MD5 and SHA-1 are:
 * <ul>
 * <li>Too fast (can be brute-forced quickly with modern hardware)</li>
 * <li>Not designed for password hashing</li>
 * <li>Vulnerable to collision attacks</li>
 * <li>Don't have built-in salt support</li>
 * </ul>
 * Use purpose-built password hashing algorithms instead.
 * </p>
 * 
 * <p>
 * <b>Q:</b> How do you migrate from one hashing algorithm to another?
 * </p>
 * <p>
 * <b>A:</b> Strategies:
 * <ul>
 * <li>Prefix-based: Store algorithm identifier with hash</li>
 * <li>Gradual migration: Rehash on next login</li>
 * <li>Dual verification: Check both old and new hashes during transition</li>
 * </ul>
 * </p>
 * 
 * @author Shivam Srivastav
 */
@Component
public class PasswordHashingAlgorithms {

    /**
     * BCrypt encoder with work factor of 12.
     * Higher work factor = more secure but slower.
     * 
     * Time complexity: O(2^workFactor)
     */
    public PasswordEncoder bcryptEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * Argon2 encoder with recommended parameters.
     * 
     * Parameters:
     * - saltLength: 16 bytes
     * - hashLength: 32 bytes
     * - parallelism: 1 (number of threads)
     * - memory: 65536 KB (64 MB)
     * - iterations: 3
     */
    public PasswordEncoder argon2Encoder() {
        return new Argon2PasswordEncoder(16, 32, 1, 65536, 3);
    }

    /**
     * PBKDF2 encoder with SHA-256 and 310,000 iterations.
     * NIST recommends at least 310,000 iterations for PBKDF2-HMAC-SHA256.
     */
    public PasswordEncoder pbkdf2Encoder() {
        return Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    }

    /**
     * Demonstrates password hashing with different algorithms.
     */
    public void demonstrateHashing(String plainPassword) {
        System.out.println("=== Password Hashing Demonstration ===\n");

        // BCrypt
        long startTime = System.nanoTime();
        String bcryptHash = bcryptEncoder().encode(plainPassword);
        long bcryptTime = System.nanoTime() - startTime;
        System.out.println("BCrypt:");
        System.out.println("  Hash: " + bcryptHash);
        System.out.println("  Time: " + bcryptTime / 1_000_000 + " ms");
        System.out.println("  Length: " + bcryptHash.length() + " characters\n");

        // Argon2
        startTime = System.nanoTime();
        String argon2Hash = argon2Encoder().encode(plainPassword);
        long argon2Time = System.nanoTime() - startTime;
        System.out.println("Argon2:");
        System.out.println("  Hash: " + argon2Hash);
        System.out.println("  Time: " + argon2Time / 1_000_000 + " ms");
        System.out.println("  Length: " + argon2Hash.length() + " characters\n");

        // PBKDF2
        startTime = System.nanoTime();
        String pbkdf2Hash = pbkdf2Encoder().encode(plainPassword);
        long pbkdf2Time = System.nanoTime() - startTime;
        System.out.println("PBKDF2:");
        System.out.println("  Hash: " + pbkdf2Hash);
        System.out.println("  Time: " + pbkdf2Time / 1_000_000 + " ms");
        System.out.println("  Length: " + pbkdf2Hash.length() + " characters\n");
    }
}
