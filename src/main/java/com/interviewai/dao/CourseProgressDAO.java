package com.interviewai.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.interviewai.model.Chapter;
import com.interviewai.model.GeneratedCourse;
import com.interviewai.model.Question;

/**
 * DAO focused on retrieving learning-path information (courses, chapters, questions)
 * and updating completion status for chapters/questions.
 */
public class CourseProgressDAO {

    private static final String ACTIVE_COURSE_SQL =
        "SELECT g.id, g.course_title, o.interview_type, o.language, g.status, g.created_at, COUNT(c.id) AS chapter_count " +
        "FROM generated_courses g " +
        "JOIN chapters c ON c.course_id = g.id " +
        "LEFT JOIN onboarding_data o ON o.user_id = g.user_id " +
        "WHERE g.user_id = ? " +
        "GROUP BY g.id, g.course_title, o.interview_type, o.language, g.status, g.created_at " +
        "HAVING COUNT(c.id) > 0 " +
        "ORDER BY chapter_count DESC, g.created_at DESC " +
        "LIMIT 1";

    private static final String CHAPTERS_SQL =
            "SELECT id, chapter_number, name, description, status " +
            "FROM chapters WHERE course_id = ? ORDER BY chapter_number ASC";

    private static final String QUESTION_COUNTS_SQL =
            "SELECT COUNT(*) AS total_questions, " +
            "SUM(CASE WHEN status = 'COMPLETED' OR status = 'INCORRECT' THEN 1 ELSE 0 END) AS completed_questions " +
            "FROM questions WHERE chapter_id = ?";

    private static final String QUESTIONS_SQL =
            "SELECT id, question, question_type, correct_answer, explanation, status " +
            "FROM questions WHERE chapter_id = ? ORDER BY id ASC";

    private static final String QUESTION_CHOICES_SQL =
            "SELECT choice_text FROM choices WHERE question_id = ? ORDER BY id ASC";

    private static final String UPDATE_CHAPTER_STATUS_SQL =
            "UPDATE chapters SET status = ?, completed_at = CASE WHEN ? = 'COMPLETED' THEN NOW() ELSE completed_at END WHERE id = ?";

    private static final String UPDATE_QUESTION_STATUS_SQL =
            "UPDATE questions SET status = ? WHERE id = ?";

    private static final String REMAINING_QUESTIONS_SQL =
            "SELECT COUNT(*) AS remaining FROM questions WHERE chapter_id = ? AND (status = 'NOT_STARTED' OR status = 'IN_PROGRESS')";

    private static final String SINGLE_CHAPTER_SQL =
            "SELECT id, course_id, chapter_number, name, description, status " +
            "FROM chapters WHERE id = ?";

    /**
     * Fetch most recent active course for the user with ordered chapters.
     */
    public Optional<GeneratedCourse> findLatestActiveCourseWithChapters(int userId) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement courseStmt = conn.prepareStatement(ACTIVE_COURSE_SQL)) {

            courseStmt.setInt(1, userId);
            try (ResultSet courseRs = courseStmt.executeQuery()) {
                if (!courseRs.next()) {
                    return Optional.empty();
                }

                GeneratedCourse course = new GeneratedCourse();
                course.setId(courseRs.getInt("id"));
                course.setUserId(userId);
                course.setCourseTitle(courseRs.getString("course_title"));
                course.setInterviewType(courseRs.getString("interview_type"));
                course.setLanguage(courseRs.getString("language"));
                course.setStatus(courseRs.getString("status"));

                List<Chapter> chapters = fetchChaptersWithCounts(conn, course.getId());
                course.setChapters(chapters);
                return Optional.of(course);
            }
        }
    }

    private List<Chapter> fetchChaptersWithCounts(Connection conn, int courseId) throws SQLException {
        List<Chapter> chapters = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(CHAPTERS_SQL)) {
            stmt.setInt(1, courseId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Chapter chapter = new Chapter();
                    chapter.setId(rs.getInt("id"));
                    chapter.setChapterNumber(rs.getInt("chapter_number"));
                    chapter.setName(rs.getString("name"));
                    chapter.setDescription(rs.getString("description"));
                    chapter.setStatus(rs.getString("status"));
                    chapter.setQuestions(Collections.emptyList());

                    Map<String, Integer> counts = fetchQuestionCounts(conn, chapter.getId());
                    chapter.setTotalQuestions(counts.getOrDefault("total", 0));
                    chapter.setCompletedQuestions(counts.getOrDefault("completed", 0));

                    chapters.add(chapter);
                }
            }
        }
        return chapters;
    }

    private Map<String, Integer> fetchQuestionCounts(Connection conn, int chapterId) throws SQLException {
        Map<String, Integer> counts = new HashMap<>();
        try (PreparedStatement stmt = conn.prepareStatement(QUESTION_COUNTS_SQL)) {
            stmt.setInt(1, chapterId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    counts.put("total", rs.getInt("total_questions"));
                    counts.put("completed", rs.getInt("completed_questions"));
                }
            }
        }
        return counts;
    }

    /**
     * Retrieve all questions (and their choices) for the given chapter.
     */
    public List<Question> getQuestionsForChapter(int chapterId) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            return getQuestionsForChapter(conn, chapterId);
        }
    }

    private List<Question> getQuestionsForChapter(Connection conn, int chapterId) throws SQLException {
        List<Question> questions = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(QUESTIONS_SQL)) {
            stmt.setInt(1, chapterId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Question question = new Question();
                    question.setId(rs.getInt("id"));
                    question.setQuestion(rs.getString("question"));
                    question.setQuestionType(rs.getString("question_type"));
                    question.setCorrectAnswer(rs.getString("correct_answer"));
                    question.setExplanation(rs.getString("explanation"));
                    question.setStatus(rs.getString("status"));
                    question.setChoices(fetchChoicesForQuestion(conn, question.getId()));
                    questions.add(question);
                }
            }
        }
        return questions;
    }

    private List<String> fetchChoicesForQuestion(Connection conn, int questionId) throws SQLException {
        List<String> choices = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(QUESTION_CHOICES_SQL)) {
            stmt.setInt(1, questionId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    choices.add(rs.getString("choice_text"));
                }
            }
        }
        return choices;
    }

    public void updateChapterStatus(int chapterId, Chapter.ChapterStatus status) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_CHAPTER_STATUS_SQL)) {
            stmt.setString(1, status.name());
            stmt.setString(2, status.name());
            stmt.setInt(3, chapterId);
            stmt.executeUpdate();
        }
    }

    public void updateQuestionStatus(int questionId, Question.QuestionStatus status) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_QUESTION_STATUS_SQL)) {
            stmt.setString(1, status.name());
            stmt.setInt(2, questionId);
            stmt.executeUpdate();
        }
    }

    public boolean areAllQuestionsCompleted(int chapterId) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(REMAINING_QUESTIONS_SQL)) {
            stmt.setInt(1, chapterId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int remaining = rs.getInt("remaining");
                    System.out.println("📊 Chapter " + chapterId + " has " + remaining + " unanswered questions (NOT_STARTED or IN_PROGRESS)");
                    return remaining == 0;
                }
            }
        }
        return false;
    }

    public Optional<Chapter> findChapterById(int chapterId) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SINGLE_CHAPTER_SQL)) {
            stmt.setInt(1, chapterId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }

                Chapter chapter = new Chapter();
                chapter.setId(rs.getInt("id"));
                chapter.setChapterNumber(rs.getInt("chapter_number"));
                chapter.setName(rs.getString("name"));
                chapter.setDescription(rs.getString("description"));
                chapter.setStatus(rs.getString("status"));
                chapter.setQuestions(Collections.emptyList());

                Map<String, Integer> counts = fetchQuestionCounts(conn, chapter.getId());
                chapter.setTotalQuestions(counts.getOrDefault("total", 0));
                chapter.setCompletedQuestions(counts.getOrDefault("completed", 0));

                return Optional.of(chapter);
            }
        }
    }

    public void ensureChapterInProgress(int chapterId) throws SQLException {
        Optional<Chapter> chapterOpt = findChapterById(chapterId);
        if (chapterOpt.isPresent()) {
            Chapter chapter = chapterOpt.get();
            if (chapter.getStatus() == Chapter.ChapterStatus.NOT_STARTED) {
                updateChapterStatus(chapterId, Chapter.ChapterStatus.IN_PROGRESS);
            }
        }
    }

    public Map<String, Integer> refreshChapterCounts(int chapterId) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            return fetchQuestionCounts(conn, chapterId);
        }
    }
    
    /**
     * Get a course by its ID with all chapters
     * @param courseId The course ID
     * @return The GeneratedCourse object or null if not found
     */
    public GeneratedCourse getCourseById(int courseId) {
        String sql = "SELECT g.id, g.user_id, g.course_title, g.status, g.created_at, " +
                     "o.interview_type, o.language " +
                     "FROM generated_courses g " +
                     "LEFT JOIN onboarding_data o ON o.user_id = g.user_id " +
                     "WHERE g.id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, courseId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    GeneratedCourse course = new GeneratedCourse();
                    course.setId(rs.getInt("id"));
                    course.setUserId(rs.getInt("user_id"));
                    course.setCourseTitle(rs.getString("course_title"));
                    course.setInterviewType(rs.getString("interview_type"));
                    course.setLanguage(rs.getString("language"));
                    course.setStatus(rs.getString("status"));
                    
                    // Load chapters
                    List<Chapter> chapters = fetchChaptersWithCounts(conn, courseId);
                    course.setChapters(chapters);
                    
                    return course;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching course by ID: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
}
