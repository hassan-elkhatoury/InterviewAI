-- Migration: Add role column to users table
-- Run this once if your database already exists without the role column

USE interviewai;

-- Add role column if it doesn't exist
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS role VARCHAR(20) DEFAULT 'CANDIDATE' AFTER password_hash;

-- Optional: Set existing users to CANDIDATE role if they have NULL
UPDATE users SET role = 'CANDIDATE' WHERE role IS NULL;
