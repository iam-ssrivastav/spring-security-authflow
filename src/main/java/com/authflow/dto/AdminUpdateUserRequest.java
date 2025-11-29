package com.authflow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Request for updating user by admin.
 * 
 * @author Shivam Srivastav
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminUpdateUserRequest {

    private String email;
    private Boolean enabled;
    private Boolean accountNonLocked;
    private Set<String> roles; // Role names
}
