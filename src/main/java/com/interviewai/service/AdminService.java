package com.interviewai.service;

import java.sql.SQLException;
import java.util.List;

import com.interviewai.dao.RoleDAO;
import com.interviewai.dao.UserDAO;
import com.interviewai.model.Role;
import com.interviewai.model.User;

public class AdminService {
    private UserDAO userDAO;
    private RoleDAO roleDAO;

    public AdminService() {
        this.userDAO = new UserDAO();
        this.roleDAO = new RoleDAO();
    }

    public User login(String username, String password) {
        try {
            if (userDAO.validateCredentials(username, password)) {
                User user = userDAO.getByUsername(username);
                if (user != null && user.isActive()) {
                    // Check if user has any admin role (not CANDIDATE)
                    boolean isAdmin = user.getRoles().stream()
                            .anyMatch(r -> !r.getName().equalsIgnoreCase("CANDIDATE"));
                    
                    if (isAdmin) {
                        return user;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean createAdmin(String username, String email, String password, String roleName) {
        try {
            return userDAO.createUser(username, email, password, roleName);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<User> getAllAdmins() {
        try {
            return userDAO.getAllAdmins();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Role> getAllRoles() {
        try {
            return roleDAO.getAllRoles();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean updateAdminRole(int userId, String roleName) {
        try {
            // Remove existing roles (assuming single role for simplicity in UI)
            List<Role> currentRoles = roleDAO.getRolesForUser(userId);
            for (Role role : currentRoles) {
                roleDAO.removeRoleFromUser(userId, role.getId());
            }
            
            Role newRole = roleDAO.getRoleByName(roleName);
            if (newRole != null) {
                roleDAO.assignRoleToUser(userId, newRole.getId());
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean toggleAdminAccess(int userId, boolean isActive) {
        try {
            User user = userDAO.getById(userId);
            if (user != null) {
                user.setActive(isActive);
                userDAO.updateUser(user);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean resetPassword(int userId, String newPassword) {
        try {
            return userDAO.updatePassword(userId, newPassword);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<User> getAllUsers() {
        try {
            return userDAO.getAllUsers();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
