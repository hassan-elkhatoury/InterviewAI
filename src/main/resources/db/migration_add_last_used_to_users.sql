-- Migration: add last used course and chapter to users
-- Adds nullable foreign keys to remember the last-used course/chapter per user

ALTER TABLE users
  ADD COLUMN last_course_id INT DEFAULT NULL;

ALTER TABLE users
  ADD COLUMN last_chapter_id INT DEFAULT NULL;

-- Add foreign key constraints (optional but helpful). If your migration
-- system doesn't support adding constraints separately remove or adapt.
ALTER TABLE users
  ADD CONSTRAINT fk_users_last_course FOREIGN KEY (last_course_id) REFERENCES generated_courses(id) ON DELETE SET NULL;

ALTER TABLE users
  ADD CONSTRAINT fk_users_last_chapter FOREIGN KEY (last_chapter_id) REFERENCES chapters(id) ON DELETE SET NULL;

-- Optional initial population: if you want to set last_course for existing
-- users to the most recent course they created, you can run a statement like:
-- UPDATE users u
-- JOIN (
--   SELECT user_id, MAX(id) as recent_course_id FROM generated_courses GROUP BY user_id
-- ) gc ON gc.user_id = u.id
-- SET u.last_course_id = gc.recent_course_id;
