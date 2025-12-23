# InterviewAI Project Analysis 📋

## 🎯 Project Overview

**InterviewAI** is a comprehensive **JavaFX-based Interview Preparation Platform** that combines structured learning paths with AI-powered feedback to help users master interviews. It's a desktop application built with Java 17, featuring a sophisticated admin system and gamification mechanics.

### Tech Stack
- **Language**: Java 17
- **UI Framework**: JavaFX 20 (FXML-based)
- **Database**: MySQL/MariaDB 8.0+
- **Build Tool**: Maven 3.9+
- **Key Libraries**:
  - Gson (JSON serialization)
  - BCrypt (password hashing)
  - Apache PDFBox & POI (document processing)
  - MySQL Connector/J (database connectivity)

---

## 📊 Architecture Overview

### Layered Architecture
```
┌─────────────────────────────────────┐
│     JavaFX UI Layer (FXML/CSS)      │
│  (Controllers & Views)              │
├─────────────────────────────────────┤
│     Service Layer                   │
│  (Business Logic)                   │
├─────────────────────────────────────┤
│     DAO Layer (Data Access)         │
│  (Database Operations)              │
├─────────────────────────────────────┤
│     MySQL Database                  │
│  (Persistent Storage)               │
└─────────────────────────────────────┘
```

### Project Structure
```
src/main/java/com/interviewai/
├── ai/              # AI-related services (prompt generation, responses)
├── controller/      # JavaFX Controllers (UI logic)
├── dao/             # Data Access Objects (database layer)
├── enums/           # Enumeration classes
├── main/            # Application entry point (MainApp)
├── model/           # Data models (POJOs)
├── service/         # Business logic services
└── util/            # Utilities (routing, session, etc.)

src/main/resources/
├── config/          # Configuration files
├── css/             # Stylesheet files
├── db/              # SQL schemas & migrations
├── fxml/            # JavaFX FXML views
└── images/          # Image assets
```

---

## 👨‍💼 ADMIN SYSTEM (RBAC Architecture)

### Overview
The admin system implements a sophisticated **Role-Based Access Control (RBAC)** with multiple admin roles, each with specific permissions. This allows granular control over who can perform what actions.

### Database Schema (Admin-Related)

#### **users Table**
```sql
CREATE TABLE users (
  id INT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(100) NOT NULL UNIQUE,
  email VARCHAR(255),
  password_hash VARCHAR(255),
  role VARCHAR(20) DEFAULT 'CANDIDATE',  -- Legacy field
  is_active TINYINT(1) DEFAULT 1,
  two_factor_enabled TINYINT(1) DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  last_course_id INT,
  last_chapter_id INT,
  last_daily_quest_claim DATE,
  last_monthly_quest_claim DATE
);
```

#### **roles Table**
```sql
CREATE TABLE roles (
  id INT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL UNIQUE,
  description TEXT
);
```

#### **permissions Table**
```sql
CREATE TABLE permissions (
  id INT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL UNIQUE,
  description TEXT
);
```

#### **user_roles Table** (Many-to-Many)
```sql
CREATE TABLE user_roles (
  user_id INT NOT NULL,
  role_id INT NOT NULL,
  PRIMARY KEY (user_id, role_id),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);
```

#### **role_permissions Table** (Many-to-Many)
```sql
CREATE TABLE role_permissions (
  role_id INT NOT NULL,
  permission_id INT NOT NULL,
  PRIMARY KEY (role_id, permission_id),
  FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
  FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
);
```

### Admin Roles

| Role | Description | Default Permissions |
|------|-------------|-------------------|
| **SUPER_ADMIN** | Full platform access | All permissions |
| **CONTENT_ADMIN** | Manage courses and content | `manage_courses` |
| **AI_MANAGER** | Manage AI prompts & settings | `manage_ai_prompts` |
| **MODERATOR** | Moderate user feedback & community | `moderate_feedback` |
| **ANALYST** | View analytics and reports | `view_analytics` |
| **CANDIDATE** | Standard user (not admin) | None |

### Default Permissions

| Permission | Description |
|-----------|-------------|
| `manage_users` | Create, edit, delete users |
| `manage_courses` | Create, edit, delete courses |
| `manage_ai_prompts` | Manage AI prompts and settings |
| `view_analytics` | View system analytics & dashboards |
| `moderate_feedback` | Moderate user feedback |

### Key Admin Models

#### **User Model**
```java
public class User {
    private int id;
    private String username;
    private String email;
    private String passwordHash;
    private boolean isActive;
    private boolean twoFactorEnabled;
    private List<Role> roles;  // Multiple roles support
    
    // Methods for permission checking
    public boolean hasRole(String roleName)
    public boolean hasPermission(String permissionName)
}
```

#### **Role Model**
```java
public class Role {
    private int id;
    private String name;
    private String description;
    private List<Permission> permissions;  // Assigned permissions
}
```

#### **Permission Model**
```java
public class Permission {
    private int id;
    private String name;
    private String description;
}
```

---

## 🖥️ Admin UI Components

### Admin Controllers

#### **1. AdminController** (Dashboard)
- **File**: [AdminController.java](src/main/java/com/interviewai/controller/AdminController.java)
- **Purpose**: Main admin dashboard with analytics and KPIs
- **Key Features**:
  - Display user metrics (total users, active users, daily/weekly/monthly signups)
  - Show course analytics (total courses, interviews completed, average score)
  - Render charts (signup trends, usage trends, interview type distribution)
  - System health monitoring (DB status, memory usage, API uptime)
  - Real-time analytics updates

**Key UI Elements**:
- KPI Cards (glass-style cards with gradient backgrounds)
- Line Charts (signup trends, usage trends)
- Pie Charts (interview type distribution, language distribution)
- System health indicators

#### **2. AdminProfileController** (Admin Account Management)
- **File**: [AdminProfileController.java](src/main/java/com/interviewai/controller/AdminProfileController.java)
- **Purpose**: Admin personal profile management
- **Key Features**:
  - View admin account information (name, email, username)
  - Edit display name
  - Change password (with validation)
  - View role and permissions
  - Track last login, session information
  - Display admin statistics (users managed, courses managed)

**Key UI Elements**:
- Profile banner (avatar, name, email, role badge)
- Account information card
- Password change section
- Admin statistics panel
- Header stats (role, last login, status)

#### **3. UserManagementController** (User Directory & Management)
- **File**: [UserManagementController.java](src/main/java/com/interviewai/controller/UserManagementController.java)
- **Purpose**: Comprehensive user management interface
- **Key Features**:
  - User directory with search and filtering
  - Filter by interview type, language, status, progress
  - Pagination for large user lists
  - User profile panel with detailed stats
  - Quick actions (enable/disable, reset password, assign roles)
  - User progress tracking
  - Delete user functionality

**Key UI Elements**:
- Search bar and filter dropdowns
- User table with columns: ID, Username, Email, Role, Status, XP, Last Active
- User profile panel (right side)
- Quick stats: XP, Streak, Badges, Lessons completed
- Action buttons (Edit, Delete, Reset Password, etc.)

#### **4. AdminSidebarController** (Navigation)
- **File**: [AdminSidebarController.java](src/main/java/com/interviewai/controller/AdminSidebarController.java)
- **Purpose**: Admin panel sidebar navigation
- **Key Features**:
  - Navigation menu for admin sections
  - Menu items: Dashboard, User Management, Analytics, AI Prompts, Content, Settings, Profile
  - Active page highlighting
  - User logout button

### Other Controllers
- **CoursesManagementController**: Manage courses and chapters
- **AnalyticsService**: Aggregates analytics data for dashboard

---

## 🔐 Admin Services & DAOs

### **AdminService**
- **File**: [AdminService.java](src/main/java/com/interviewai/service/AdminService.java)
- **Purpose**: Business logic for admin operations
- **Key Methods**:
  - `login(username, password)`: Authenticate admin users (checks for non-CANDIDATE roles)
  - `createAdmin(username, email, password, roleName)`: Create new admin
  - `getAllAdmins()`: Fetch all admin users
  - `getAllRoles()`: Get available roles
  - `updateAdminRole(userId, roleName)`: Change user's role
  - `deactivateAdmin(userId)`: Deactivate admin account

### **AnalyticsService**
- **File**: [AnalyticsService.java](src/main/java/com/interviewai/service/AnalyticsService.java)
- **Purpose**: Aggregates and calculates analytics metrics
- **Key Methods**:
  - `getDashboardMetrics()`: Comprehensive dashboard data
  - `getUserMetrics()`: User-related statistics
  - `getCourseMetrics()`: Course and interview metrics
  - `getSystemHealthMetrics()`: System performance data

### **UserDAO**
- **File**: [UserDAO.java](src/main/java/com/interviewai/dao/UserDAO.java)
- **Purpose**: User database operations
- **Key Methods**:
  - `createUser(username, email, password, roleName)`: Create user with role assignment
  - `getByUsername(username)`: Fetch user by username
  - `getByEmail(email)`: Fetch user by email
  - `getAllAdmins()`: Get all non-CANDIDATE users
  - `validateCredentials(username, password)`: Verify login credentials
  - `updateUser(user)`: Update user information
  - `updatePassword(userId, newPasswordHash)`: Change password
  - `deactivateUser(userId)`: Deactivate user account

### **RoleDAO**
- **File**: [RoleDAO.java](src/main/java/com/interviewai/dao/RoleDAO.java)
- **Purpose**: Role and permission management
- **Key Methods**:
  - `getRoleByName(roleName)`: Fetch role with permissions
  - `getRolesForUser(userId)`: Get user's roles
  - `getPermissionsForRole(roleId)`: Get role's permissions
  - `assignRoleToUser(userId, roleId)`: Assign role to user
  - `removeRoleFromUser(userId, roleId)`: Remove role from user
  - `getAllRoles()`: Get all available roles

### **UserManagementDAO**
- **File**: [UserManagementDAO.java](src/main/java/com/interviewai/dao/UserManagementDAO.java)
- **Purpose**: User management specific queries
- **Key Methods**:
  - `searchUsers(query, filters)`: Search with filtering
  - `getUsersWithPagination(offset, limit)`: Paginated user lists
  - `countTotalUsers()`: Get user count
  - `getUserStats(userId)`: Detailed user statistics

### **AnalyticsDAO**
- **File**: [AnalyticsDAO.java](src/main/java/com/interviewai/dao/AnalyticsDAO.java)
- **Purpose**: Analytics and metrics queries
- **Key Methods**:
  - `getTotalUsers()`: Total registered users
  - `getActiveUsers()`: Currently active users
  - `getDailyActiveUsers(days)`: DAU metrics
  - `getTodaySignups()`, `getThisWeekSignups()`, `getThisMonthSignups()`: Signup trends
  - `getTotalCourses()`: Total generated courses
  - `getTotalInterviewsCompleted()`: Interview completion count
  - `getSignupTrend(days)`: Historical signup data
  - `getUsageTrend(days)`: Platform usage trends
  - `getInterviewTypeDistribution()`: Interview type statistics

---

## 🎨 Admin UI Styling

### CSS Files
- **admin.css**: Main admin dashboard styling (glass cards, charts, KPIs)
- **admin-profile.css**: Admin profile page styling
- **admin-sidebar.css**: Sidebar navigation styling (shared component)

### Design Elements
- **Glass-morphism cards**: Semi-transparent, blurred backgrounds
- **Gradient backgrounds**: Multi-color gradients for visual appeal
- **Color-coded stat cards**: Different colors for different metrics (blue, green, purple, orange)
- **Responsive layout**: Flexbox-based layouts for different screen sizes
- **Live indicators**: Animated pulse dots for real-time status

---

## 📊 Admin Features in Detail

### Dashboard Analytics
The admin dashboard displays comprehensive metrics:

**User Metrics**:
- Total users (ever registered)
- Active users (currently online)
- Daily, weekly, and monthly signups

**Activity Metrics**:
- Interviews completed (total)
- Average score
- Completed chapters
- In-progress chapters

**Trends**:
- Signup trends (30-day line chart)
- Usage trends (30-day line chart)
- Interview type distribution (pie chart)
- Language selection distribution (pie chart)

**System Health**:
- Database status (connected/disconnected)
- Memory usage (progress bar)
- AI response time
- API uptime

### User Management Features
- **Search**: Full-text search across username and email
- **Filtering**: By interview type, language, status, progress level
- **Pagination**: Browse users in manageable chunks
- **User Profile**: Detailed view of individual user stats and progress
- **Quick Actions**: 
  - View user details
  - Edit user information
  - Reset password
  - Assign roles
  - Enable/disable account
  - Delete user

### Admin Profile Management
- **Account Info**: Edit display name, view email, username
- **Security**: Change password with current password validation
- **Role Management**: View assigned role and permissions
- **Statistics**: Track users managed, courses managed, session info
- **Login History**: Last login time and IP address

---

## 🔄 Data Flow & Request Flow

### Admin Login Flow
```
1. Admin enters username/password on Login screen
2. LoginController → AdminService.login()
3. AdminService queries UserDAO.validateCredentials()
4. UserDAO verifies password using BCrypt
5. If valid, UserDAO.getByUsername() fetches user with roles
6. AdminService checks if user has non-CANDIDATE roles
7. SessionContext.setCurrentUser() stores session
8. Redirect to AdminController (dashboard)
```

### User Management Flow
```
1. Admin clicks "User Management" in sidebar
2. UserManagementController.initialize()
3. Loads users from UserManagementDAO with pagination
4. Loads filter options (interview types, languages, statuses)
5. Displays user table with search capability
6. On user click: Load detailed user stats via ProgressDAO, etc.
7. On action (edit/delete/reset): Call appropriate DAO method
```

### Permission Checking
```
1. User.hasRole(roleName) → Check if user has role
2. User.hasPermission(permissionName) → Check roles' permissions
3. RoleDAO fetches roles & permissions from database
4. Controllers can restrict UI elements based on permissions
5. Example: Only SUPER_ADMIN can delete users
```

---

## 🛠️ Key Admin Classes & Their Relationships

```
User (model)
├── has 0..* → Role (model)
│               └── has 0..* → Permission (model)
└── uses ↓
    UserDAO (dao)
    ├── queries → users table
    ├── uses ↓
    │   RoleDAO (dao)
    │   ├── queries → roles, user_roles, permissions, role_permissions
    │   └── populates Role objects with permissions
    │
    ├── AdminService (service)
    │   ├── calls UserDAO methods
    │   ├── calls RoleDAO methods
    │   └── used by AdminController
    │
    └── UserManagementDAO (dao)
        ├── queries → users, onboarding_data, progress
        └── used by UserManagementController
```

---

## 🚀 Admin Features Summary

| Feature | Controller | DAO | Service | Status |
|---------|-----------|-----|---------|--------|
| Dashboard Analytics | AdminController | AnalyticsDAO | AnalyticsService | ✅ Active |
| User Directory | UserManagementController | UserManagementDAO | - | ✅ Active |
| User Search & Filter | UserManagementController | UserManagementDAO | - | ✅ Active |
| Admin Profile | AdminProfileController | UserDAO | - | ✅ Active |
| Role Management | AdminController | RoleDAO | AdminService | ✅ Active |
| Permission Checking | Various | RoleDAO | - | ✅ Active |
| Course Management | CoursesManagementController | CourseDAO | - | ✅ Active |
| Analytics Reports | AdminController | AnalyticsDAO | AnalyticsService | ✅ Active |

---

## 🔑 Key Insights

### Strengths
1. **Robust RBAC System**: Multiple role levels with granular permissions
2. **Comprehensive Analytics**: Real-time dashboard with multiple metrics
3. **User Management**: Full CRUD operations with advanced filtering
4. **Security**: BCrypt password hashing, permission-based access control
5. **Modern UI**: Glass-morphism design with JavaFX FXML

### Areas for Enhancement
1. **Audit Logging**: Could track admin actions (who changed what and when)
2. **Email Notifications**: Admin alerts for critical events
3. **Batch Operations**: Bulk user management (delete, role assignment)
4. **Advanced Reports**: Custom report generation capabilities
5. **Admin Activity Log**: History of admin changes to system

---

## 📝 Current Database State

### Existing Users
- **User 1**: `hassan` (CANDIDATE role)
- **User 2**: `moad` (ADMIN role)

### Database Statistics
- **Total Users**: 2
- **Total Courses**: 4
- **Total Chapters**: 24
- **Interview Categories**: 2 (Technical + Behavioral)

---

## 🎓 Non-Admin Features (Context)

### Main Application Features
- **6-Stage Learning Path**: Self-intro → Strengths → Weaknesses → Technical → HR → Mock Interview
- **Gamification**: XP points, streaks, badges, daily missions, leaderboards
- **Progress Tracking**: Real-time metrics and analytics for users
- **AI-Powered Feedback**: Personalized coaching and feedback
- **Course Generation**: AI-generated interview prep courses
- **Onboarding**: Interview type, language, timeline, and context collection
- **Interview Simulation**: Mock interview feature with scoring

---

## 📚 Related Files Reference

### Core Admin Files
- Controllers: `AdminController`, `AdminProfileController`, `UserManagementController`, `AdminSidebarController`
- Services: `AdminService`, `AnalyticsService`
- DAOs: `UserDAO`, `RoleDAO`, `UserManagementDAO`, `AnalyticsDAO`
- Models: `User`, `Role`, `Permission`
- Views: `AdminView.fxml`, `AdminProfileView.fxml`, `UserManagementView.fxml`, `components/AdminSidebar.fxml`
- Styles: `admin.css`, `admin-profile.css`, `admin-sidebar.css`

### Database Files
- Schema: `schema.sql` (413 lines)
- Migration: `migration_admin_rbac.sql` (78 lines)

---

## 🏃 How to Run Admin Features

```bash
# 1. Compile the project
mvn compile

# 2. Run the application
mvn javafx:run

# 3. Login with admin account
# Username: moad
# Password: (check BCrypt hash in database)

# 4. Access admin dashboard
# - User Management
# - Analytics Dashboard
# - Admin Profile
# - Courses Management
```

---

*Analysis created on December 24, 2025. For the latest information, refer to the actual source files.*
