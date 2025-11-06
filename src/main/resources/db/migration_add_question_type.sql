-- Migration script to add question_type column to questions table
-- Run this script manually in your MySQL database

USE interviewai;

-- Add question_type column with default value
ALTER TABLE questions 
ADD COLUMN question_type VARCHAR(50) DEFAULT 'MULTIPLE_CHOICE' AFTER question;

-- Update existing records to have MULTIPLE_CHOICE type
UPDATE questions SET question_type = 'MULTIPLE_CHOICE' WHERE question_type IS NULL;

-- Verify the change
SELECT * FROM questions LIMIT 5;
