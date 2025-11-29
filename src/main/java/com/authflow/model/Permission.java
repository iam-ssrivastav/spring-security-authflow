package com.authflow.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Permission entity for fine-grained access control.
 * 
 * <p>
 * Permissions represent specific actions that can be performed on resources.
 * This enables Permission-Based Access Control (PBAC) which is more granular
 * than RBAC.
 * </p>
 * 
 * <h2>Interview Q&A:</h2>
 * <p>
 * <b>Q:</b> What's the difference between roles and permissions?
 * </p>
 * <p>
 * <b>A:</b> Roles are collections of permissions. A user has roles, and roles
 * have
 * permissions. This provides flexibility:
 * <ul>
 * <li>Permissions: READ_DOCUMENT, WRITE_DOCUMENT, DELETE_DOCUMENT</li>
 * <li>Role EDITOR: Has READ_DOCUMENT and WRITE_DOCUMENT</li>
 * <li>Role ADMIN: Has all three permissions</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <b>Q:</b> How do you handle the "role explosion" problem?
 * </p>
 * <p>
 * <b>A:</b> Use permission-based access control instead of creating many roles.
 * Assign permissions directly or group them logically. Consider ABAC for
 * complex scenarios.
 * </p>
 * 
 * @author Shivam Srivastav
 */
@Entity
@Table(name = "permissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    @Enumerated(EnumType.STRING)
    private PermissionName name;

    private String description;

    /**
     * Resource type this permission applies to.
     * Enables resource-based authorization.
     */
    private String resourceType; // DOCUMENT, USER, REPORT, etc.

    /**
     * Predefined permission names following CRUD + Execute pattern.
     */
    public enum PermissionName {
        // Document permissions
        READ_DOCUMENT,
        WRITE_DOCUMENT,
        DELETE_DOCUMENT,
        SHARE_DOCUMENT,

        // User management permissions
        READ_USER,
        CREATE_USER,
        UPDATE_USER,
        DELETE_USER,

        // Report permissions
        READ_REPORT,
        CREATE_REPORT,
        EXPORT_REPORT,

        // System permissions
        MANAGE_ROLES,
        MANAGE_PERMISSIONS,
        VIEW_AUDIT_LOG,
        EXECUTE_ADMIN_TASKS
    }
}
