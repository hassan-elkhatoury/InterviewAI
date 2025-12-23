package com.interviewai.model;

import java.util.ArrayList;
import java.util.List;

public class User {
    private int id;
    private String username;
    private String email;
    private String passwordHash;
    private boolean isActive;
    private boolean twoFactorEnabled;
    private List<Role> roles = new ArrayList<>();

    public User() {}

    public User(int id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.isActive = true;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public boolean isTwoFactorEnabled() { return twoFactorEnabled; }
    public void setTwoFactorEnabled(boolean twoFactorEnabled) { this.twoFactorEnabled = twoFactorEnabled; }

    public List<Role> getRoles() { return roles; }
    public void setRoles(List<Role> roles) { this.roles = roles; }

    public void addRole(Role role) {
        this.roles.add(role);
    }
    
    public boolean hasRole(String roleName) {
        for (Role role : roles) {
            if (role.getName().equalsIgnoreCase(roleName)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasPermission(String permissionName) {
        for (Role role : roles) {
            for (Permission permission : role.getPermissions()) {
                if (permission.getName().equalsIgnoreCase(permissionName)) {
                    return true;
                }
            }
        }
        return false;
    }

    // Compatibility methods for legacy code
    public String getRole() {
        if (roles != null && !roles.isEmpty()) {
            return roles.get(0).getName();
        }
        return "CANDIDATE";
    }

    public void setRole(String roleName) {
        // This is a bit tricky as we don't have access to RoleDAO here to fetch ID.
        // For now, we can create a dummy Role object or just ignore if we are moving away from this.
        // However, to fix compilation, we can add a Role object with just the name.
        // Ideally, the caller should use addRole(Role) or the service layer should handle this.
        if (this.roles == null) {
            this.roles = new ArrayList<>();
        }
        // Clear existing roles to mimic old behavior of single role
        this.roles.clear();
        Role role = new Role();
        role.setName(roleName);
        this.roles.add(role);
    }
}
