package com.authflow.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

/**
 * Role entity for Role-Based Access Control (RBAC).
 * 
 * <h2>Interview Q&A:</h2>
 * <p>
 * <b>Q:</b> What is RBAC and why is it important?
 * </p>
 * <p>
 * <b>A:</b> Role-Based Access Control (RBAC) is an authorization model where
 * permissions
 * are assigned to roles, and roles are assigned to users. Benefits include:
 * <ul>
 * <li>Simplified permission management</li>
 * <li>Reduced administrative overhead</li>
 * <li>Easier compliance and auditing</li>
 * <li>Principle of least privilege enforcement</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <b>Q:</b> What is the difference between RBAC and ABAC?
 * </p>
 * <p>
 * <b>A:</b> RBAC uses predefined roles (e.g., ADMIN, USER), while ABAC
 * (Attribute-Based
 * Access Control) uses attributes of the user, resource, and environment to
 * make dynamic
 * authorization decisions. ABAC is more flexible but more complex.
 * </p>
 * 
 * @author Shivam Srivastav
 */
@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    @Enumerated(EnumType.STRING)
    private RoleName name;

    private String description;

    /**
     * Permissions associated with this role.
     * Demonstrates fine-grained permission-based access control.
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "role_permissions", joinColumns = @JoinColumn(name = "role_id"), inverseJoinColumns = @JoinColumn(name = "permission_id"))
    @Builder.Default
    private Set<Permission> permissions = new HashSet<>();

    /**
     * Role hierarchy level (higher number = more privileges).
     * Used for hierarchical RBAC.
     */
    private Integer hierarchyLevel;

    /**
     * Predefined role names.
     * 
     * <p>
     * <b>Interview Tip:</b> Role hierarchy is important:
     * SUPER_ADMIN > ADMIN > MANAGER > USER > GUEST
     * </p>
     */
    public enum RoleName {
        ROLE_SUPER_ADMIN, // Level 5 - Full system access
        ROLE_ADMIN, // Level 4 - Administrative access
        ROLE_MANAGER, // Level 3 - Management access
        ROLE_USER, // Level 2 - Standard user access
        ROLE_GUEST // Level 1 - Limited read-only access
    }
}
