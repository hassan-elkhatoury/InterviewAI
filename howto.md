## to export your db updates
mysqldump --default-character-set=utf8mb4 -u root -p --port=3306 interviewai > src\main\resources\db\schema.sql
