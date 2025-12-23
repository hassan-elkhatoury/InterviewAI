-- Add new columns to users
ALTER TABLE users ADD COLUMN is_active BOOLEAN DEFAULT TRUE;
ALTER TABLE users ADD COLUMN two_factor_enabled BOOLEAN DEFAULT FALSE;

-- Create roles table
CREATE TABLE roles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT
);

-- Create permissions table
CREATE TABLE permissions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT
);

-- Create role_permissions table
CREATE TABLE role_permissions (
    role_id INT NOT NULL,
    permission_id INT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
);

-- Create user_roles table
CREATE TABLE user_roles (
    user_id INT NOT NULL,
    role_id INT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Insert default roles
INSERT INTO roles (name, description) VALUES 
('SUPER_ADMIN', 'Full access to all features'),
('CONTENT_ADMIN', 'Manage courses and content'),
('AI_MANAGER', 'Manage AI prompts and settings'),
('MODERATOR', 'Moderate user feedback and community'),
('ANALYST', 'View analytics and reports'),
('CANDIDATE', 'Standard user');

-- Insert default permissions
INSERT INTO permissions (name, description) VALUES 
('manage_users', 'Create, edit, delete users'),
('manage_courses', 'Create, edit, delete courses'),
('manage_ai_prompts', 'Manage AI prompts'),
('view_analytics', 'View system analytics'),
('moderate_feedback', 'Moderate user feedback');

-- Assign permissions to roles
-- SUPER_ADMIN gets all
INSERT INTO role_permissions (role_id, permission_id) 
SELECT r.id, p.id FROM roles r, permissions p WHERE r.name = 'SUPER_ADMIN';

-- CONTENT_ADMIN
INSERT INTO role_permissions (role_id, permission_id) 
SELECT r.id, p.id FROM roles r, permissions p WHERE r.name = 'CONTENT_ADMIN' AND p.name IN ('manage_courses');

-- AI_MANAGER
INSERT INTO role_permissions (role_id, permission_id) 
SELECT r.id, p.id FROM roles r, permissions p WHERE r.name = 'AI_MANAGER' AND p.name IN ('manage_ai_prompts');

-- MODERATOR
INSERT INTO role_permissions (role_id, permission_id) 
SELECT r.id, p.id FROM roles r, permissions p WHERE r.name = 'MODERATOR' AND p.name IN ('moderate_feedback');

-- ANALYST
INSERT INTO role_permissions (role_id, permission_id) 
SELECT r.id, p.id FROM roles r, permissions p WHERE r.name = 'ANALYST' AND p.name IN ('view_analytics');

-- Migrate existing users to roles
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u JOIN roles r ON r.name = CASE WHEN u.role = 'ADMIN' THEN 'SUPER_ADMIN' ELSE 'CANDIDATE' END;
