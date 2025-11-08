-- Migration script to add question_type column to questions table
-- Run this script manually in your MySQL database

USE interviewai;

CREATE TABLE IF NOT EXISTS `interview_prep` (
	`id` int NOT NULL AUTO_INCREMENT,
	`user_id` int NOT NULL,
	`technical_course_id` int DEFAULT NULL,
	`softskills_course_id` int DEFAULT NULL,
	`created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	PRIMARY KEY (`id`),
	KEY `idx_user_id` (`user_id`),
	KEY `idx_technical_course_id` (`technical_course_id`),
	KEY `idx_softskills_course_id` (`softskills_course_id`),
	CONSTRAINT `interview_prep_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
	CONSTRAINT `interview_prep_ibfk_2` FOREIGN KEY (`technical_course_id`) REFERENCES `generated_courses` (`id`) ON DELETE SET NULL,
	CONSTRAINT `interview_prep_ibfk_3` FOREIGN KEY (`softskills_course_id`) REFERENCES `generated_courses` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;