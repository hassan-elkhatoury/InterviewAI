-- Migration: All Schema Updates
-- This file documents all SQL schema modifications identified in the current application state.

-- 1. Users Table Updates (Quest Tracking)
-- Tracks the last date a user claimed their daily/monthly quest rewards.
ALTER TABLE users ADD COLUMN IF NOT EXISTS last_daily_quest_claim DATE NULL;
ALTER TABLE users ADD COLUMN IF NOT EXISTS last_monthly_quest_claim DATE NULL;

-- 2. Chapters Table Update (Progress Tracking)
-- Tracks when a chapter was completed.
ALTER TABLE chapters ADD COLUMN IF NOT EXISTS completed_at DATETIME NULL;

-- 3. User Goals Table (Custom Goals Feature)
-- Recreates the table to support the final schema including 'start_value' for incremental tracking.
DROP TABLE IF EXISTS user_goals;

CREATE TABLE user_goals (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    goal_name VARCHAR(255) NOT NULL,
    goal_type VARCHAR(50) NOT NULL,
    target_value INT NOT NULL,
    start_value INT DEFAULT 0, -- Captures the user's metric at goal creation time
    current_value INT DEFAULT 0,
    is_active BOOLEAN DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
