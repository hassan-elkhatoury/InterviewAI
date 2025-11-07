-- Migration script to add status tracking columns to chapters and questions tables
-- Run these statements against your MySQL database after selecting the interviewai schema

-- Add status column to chapters table if it does not already exist
ALTER TABLE chapters
    ADD status VARCHAR(30) NOT NULL DEFAULT 'NOT_STARTED';

-- Add status column to questions table if it does not already exist
ALTER TABLE questions
    ADD status VARCHAR(30) NOT NULL DEFAULT 'NOT_STARTED';
