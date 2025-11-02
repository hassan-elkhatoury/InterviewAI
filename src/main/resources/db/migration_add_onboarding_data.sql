-- Migration script to add onboarding_data table
-- Run this if you already have an existing InterviewAI database

USE interviewai;

-- Create onboarding_data table
CREATE TABLE IF NOT EXISTS onboarding_data (
  user_id INT PRIMARY KEY,
  interview_type VARCHAR(50) NOT NULL COMMENT 'JOB, VISA, INTERNSHIP, UNIVERSITY',
  language VARCHAR(50) NOT NULL COMMENT 'ENGLISH, FRENCH, ARABIC, SPANISH',
  timeline VARCHAR(50) NOT NULL COMMENT 'TOMORROW, THIS_WEEK, LATER',
  context TEXT COMMENT 'Industry, program, or position details',
  cv_path VARCHAR(500) COMMENT 'File path to uploaded CV/resume',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Stores user onboarding preferences for personalized AI';

-- Add index for faster lookups
CREATE INDEX idx_interview_type ON onboarding_data(interview_type);
CREATE INDEX idx_language ON onboarding_data(language);

-- Sample query to verify table creation
-- SELECT * FROM onboarding_data;
