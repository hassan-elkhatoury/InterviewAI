package com.interviewai.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.interviewai.model.Question;

/**
 * Data Access Object for managing questions
 */
public class QuestionDAO {

    /**
     * Get all questions for a specific chapter
     */
    public List<Question> getQuestionsByChapterId(int chapterId) throws SQLException {
        List<Question> questions = new ArrayList<>();
        
        // First, load all questions
        String questionSql = "SELECT id, question, question_type, correct_answer, explanation, status " +
                            "FROM questions WHERE chapter_id = ? ORDER BY id";
        
        List<QuestionData> questionDataList = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(questionSql)) {
            
            stmt.setInt(1, chapterId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    QuestionData data = new QuestionData();
                    data.id = rs.getInt("id");
                    data.questionText = rs.getString("question");
                    data.questionType = rs.getString("question_type");
                    data.correctAnswer = rs.getString("correct_answer");
                    data.explanation = rs.getString("explanation");
                    data.status = rs.getString("status");
                    questionDataList.add(data);
                }
            }
        }
        
        // Now load choices for each question
        for (QuestionData data : questionDataList) {
            List<String> choices = getChoicesForQuestion(data.id);
            
            Question question = new Question(
                data.id,
                data.questionText,
                Question.typeFromString(data.questionType),
                choices,
                data.correctAnswer,
                data.explanation
            );
            question.setStatus(data.status);
            questions.add(question);
        }

        return questions;
    }
    
    // Helper class to temporarily store question data
    private static class QuestionData {
        int id;
        String questionText;
        String questionType;
        String correctAnswer;
        String explanation;
        String status;
    }

    /**
     * Get choices for a specific question
     */
    private List<String> getChoicesForQuestion(int questionId) throws SQLException {
        List<String> choices = new ArrayList<>();
        String sql = "SELECT choice_text FROM choices WHERE question_id = ? ORDER BY id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, questionId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    choices.add(rs.getString("choice_text"));
                }
            }
        }

        return choices;
    }

    /**
     * Update question status and timestamp
     */
    public void updateQuestionStatus(int questionId, Question.QuestionStatus status) throws SQLException {
        String sql = "UPDATE questions SET status = ?, updated_at = NOW() WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status.name());
            stmt.setInt(2, questionId);
            stmt.executeUpdate();
        }
    }

    /**
     * Get a single question by ID
     */
    public Question getQuestionById(int questionId) throws SQLException {
        String sql = "SELECT id, question, question_type, correct_answer, explanation, status, chapter_id " +
                     "FROM questions WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, questionId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String questionText = rs.getString("question");
                    String questionType = rs.getString("question_type");
                    String correctAnswer = rs.getString("correct_answer");
                    String explanation = rs.getString("explanation");
                    String status = rs.getString("status");

                    List<String> choices = getChoicesForQuestion(questionId);

                    Question question = new Question(
                        questionId,
                        questionText,
                        Question.typeFromString(questionType),
                        choices,
                        correctAnswer,
                        explanation
                    );
                    question.setStatus(status);
                    return question;
                }
            }
        }

        return null;
    }


    /**
     * Get all incorrect questions for a specific course
     * @param courseId The course ID to get incorrect questions from
     * @return List of questions with INCORRECT status
     */
    public List<Question> getIncorrectQuestionsByCourseId(int courseId) throws SQLException {
        List<Question> questions = new ArrayList<>();
        
        String sql = "SELECT q.id, q.question, q.question_type, q.correct_answer, q.explanation, q.status, q.chapter_id " +
                     "FROM questions q " +
                     "INNER JOIN chapters c ON q.chapter_id = c.id " +
                     "WHERE c.course_id = ? AND q.status = 'INCORRECT' " +
                     "ORDER BY q.chapter_id, q.id";
        
        List<QuestionData> questionDataList = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, courseId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    QuestionData data = new QuestionData();
                    data.id = rs.getInt("id");
                    data.questionText = rs.getString("question");
                    data.questionType = rs.getString("question_type");
                    data.correctAnswer = rs.getString("correct_answer");
                    data.explanation = rs.getString("explanation");
                    data.status = rs.getString("status");
                    questionDataList.add(data);
                }
            }
        }
        
        // Load choices for each question
        for (QuestionData data : questionDataList) {
            List<String> choices = getChoicesForQuestion(data.id);
            
            Question question = new Question(
                data.id,
                data.questionText,
                Question.typeFromString(data.questionType),
                choices,
                data.correctAnswer,
                data.explanation
            );
            question.setStatus(data.status);
            questions.add(question);
        }

        return questions;
    }

    /**
     * Get all incorrect questions for a specific chapter
     * @param chapterId The chapter ID to get incorrect questions from
     * @return List of questions with INCORRECT status
     */
    public List<Question> getIncorrectQuestionsByChapterId(int chapterId) throws SQLException {
        List<Question> questions = new ArrayList<>();
        
        String sql = "SELECT id, question, question_type, correct_answer, explanation, status " +
                     "FROM questions WHERE chapter_id = ? AND status = 'INCORRECT' " +
                     "ORDER BY id";
        
        List<QuestionData> questionDataList = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, chapterId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    QuestionData data = new QuestionData();
                    data.id = rs.getInt("id");
                    data.questionText = rs.getString("question");
                    data.questionType = rs.getString("question_type");
                    data.correctAnswer = rs.getString("correct_answer");
                    data.explanation = rs.getString("explanation");
                    data.status = rs.getString("status");
                    questionDataList.add(data);
                }
            }
        }
        
        // Load choices for each question
        for (QuestionData data : questionDataList) {
            List<String> choices = getChoicesForQuestion(data.id);
            
            Question question = new Question(
                data.id,
                data.questionText,
                Question.typeFromString(data.questionType),
                choices,
                data.correctAnswer,
                data.explanation
            );
            question.setStatus(data.status);
            questions.add(question);
        }

        return questions;
    }

   
}
