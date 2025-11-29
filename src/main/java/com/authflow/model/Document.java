package com.authflow.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Document entity for demonstrating resource-based authorization.
 * 
 * <p>
 * This entity shows how to implement owner-based access control where
 * users can only access resources they own or have been granted access to.
 * </p>
 * 
 * <h2>Interview Q&A:</h2>
 * <p>
 * <b>Q:</b> What is resource-based authorization?
 * </p>
 * <p>
 * <b>A:</b> It's an authorization model where access decisions are based on
 * the relationship between the user and the specific resource instance. For
 * example:
 * <ul>
 * <li>A user can edit their own documents but not others'</li>
 * <li>A manager can view documents in their department</li>
 * <li>An admin can access all documents</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <b>Q:</b> How do you implement resource-based authorization in Spring
 * Security?
 * </p>
 * <p>
 * <b>A:</b> Use @PreAuthorize with SpEL expressions that check resource
 * ownership:
 * {@code @PreAuthorize("@documentSecurity.isOwner(#id, authentication)")}
 * </p>
 * 
 * @author Shivam Srivastav
 */
@Entity
@Table(name = "documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    /**
     * Owner of the document - used for resource-based authorization.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    /**
     * Visibility level for ABAC policies.
     */
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Visibility visibility = Visibility.PRIVATE;

    /**
     * Department attribute for ABAC.
     */
    private String department;

    /**
     * Classification level for ABAC.
     */
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Classification classification = Classification.INTERNAL;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum Visibility {
        PUBLIC, // Anyone can view
        INTERNAL, // Only authenticated users
        PRIVATE // Only owner and admins
    }

    public enum Classification {
        PUBLIC,
        INTERNAL,
        CONFIDENTIAL,
        SECRET
    }
}
