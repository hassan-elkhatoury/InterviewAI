package com.interviewai.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.interviewai.model.Permission;
import com.interviewai.model.Role;

public class RoleDAO {

    public Role getRoleByName(String roleName) throws SQLException {
        String sql = "SELECT id, name, description FROM roles WHERE name = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, roleName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Role role = new Role(rs.getInt("id"), rs.getString("name"), rs.getString("description"));
                    role.setPermissions(getPermissionsForRole(role.getId()));
                    return role;
                }
            }
        }
        return null;
    }

    public List<Role> getRolesForUser(int userId) throws SQLException {
        List<Role> roles = new ArrayList<>();
        String sql = "SELECT r.id, r.name, r.description FROM roles r " +
                     "JOIN user_roles ur ON r.id = ur.role_id " +
                     "WHERE ur.user_id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Role role = new Role(rs.getInt("id"), rs.getString("name"), rs.getString("description"));
                    role.setPermissions(getPermissionsForRole(role.getId()));
                    roles.add(role);
                }
            }
        }
        return roles;
    }

    public List<Permission> getPermissionsForRole(int roleId) throws SQLException {
        List<Permission> permissions = new ArrayList<>();
        String sql = "SELECT p.id, p.name, p.description FROM permissions p " +
                     "JOIN role_permissions rp ON p.id = rp.permission_id " +
                     "WHERE rp.role_id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, roleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    permissions.add(new Permission(rs.getInt("id"), rs.getString("name"), rs.getString("description")));
                }
            }
        }
        return permissions;
    }
    
    public void assignRoleToUser(int userId, int roleId) throws SQLException {
        String sql = "INSERT INTO user_roles (user_id, role_id) VALUES (?, ?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, roleId);
            ps.executeUpdate();
        }
    }
    
    public void removeRoleFromUser(int userId, int roleId) throws SQLException {
        String sql = "DELETE FROM user_roles WHERE user_id = ? AND role_id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, roleId);
            ps.executeUpdate();
        }
    }
    
    public List<Role> getAllRoles() throws SQLException {
        List<Role> roles = new ArrayList<>();
        String sql = "SELECT id, name, description FROM roles";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Role role = new Role(rs.getInt("id"), rs.getString("name"), rs.getString("description"));
                // Not fetching permissions here for performance, fetch on demand if needed
                roles.add(role);
            }
        }
        return roles;
    }
}
