package com.interviewai.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.interviewai.model.Chapter;
import com.interviewai.model.GeneratedCourse;
import com.interviewai.model.Question;

public class CourseDAO {
    // Save a generated course for a user
    public Boolean saveGeneratedCourse(GeneratedCourse course) {
        boolean isSaved = false;
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // Insert course
            String sqlCourse = "INSERT INTO generated_courses (user_id, course_title, status, created_at) VALUES (?, ?, ?, NOW())";
            try (PreparedStatement psCourse = conn.prepareStatement(sqlCourse, Statement.RETURN_GENERATED_KEYS)) {
                psCourse.setInt(1, course.getUserId());
                psCourse.setString(2, course.getCourseTitle());
                psCourse.setString(3, course.getStatus());
                psCourse.executeUpdate();
                try (ResultSet rsCourse = psCourse.getGeneratedKeys()) {
                    int courseId = -1;
                    if (rsCourse.next()) {
                        courseId = rsCourse.getInt(1);
                    }

                    // Insert chapters
                    String sqlChapter = "INSERT INTO chapters (course_id, chapter_number, name, description, status) VALUES (?, ?, ?, ?, ?)";
                    try (PreparedStatement psChapter = conn.prepareStatement(sqlChapter, Statement.RETURN_GENERATED_KEYS)) {
                        for (Chapter chapter : course.getChapters()) {
                            psChapter.setInt(1, courseId);
                            psChapter.setInt(2, chapter.getChapterNumber());
                            psChapter.setString(3, chapter.getName());
                            psChapter.setString(4, chapter.getDescription());
                            psChapter.setString(5, chapter.getStatusValue());
                            psChapter.executeUpdate();
                            try (ResultSet rsChapter = psChapter.getGeneratedKeys()) {
                                int chapterId = -1;
                                if (rsChapter.next()) {
                                    chapterId = rsChapter.getInt(1);
                                }

                                // Insert questions
                                String sqlQuestion = "INSERT INTO questions (chapter_id, question, question_type, correct_answer, explanation, status) VALUES (?, ?, ?, ?, ?, ?)";
                                try (PreparedStatement psQuestion = conn.prepareStatement(sqlQuestion, Statement.RETURN_GENERATED_KEYS)) {
                                    for (Question q : chapter.getQuestions()) {
                                        psQuestion.setInt(1, chapterId);
                                        psQuestion.setString(2, q.getQuestion());
                                        psQuestion.setString(3, q.getQuestionType().name());
                                        psQuestion.setString(4, q.getCorrectAnswer());
                                        psQuestion.setString(5, q.getExplanation());
                                        psQuestion.setString(6, q.getStatusValue());
                                        psQuestion.executeUpdate();
                                        try (ResultSet rsQuestion = psQuestion.getGeneratedKeys()) {
                                            int questionId = -1;
                                            if (rsQuestion.next()) {
                                                questionId = rsQuestion.getInt(1);
                                            }

                                            // Insert choices (only for multiple choice questions)
                                            if (q.getQuestionType() == Question.QuestionType.MULTIPLE_CHOICE && q.getChoices() != null) {
                                                String sqlChoice = "INSERT INTO choices (question_id, choice_text) VALUES (?, ?)";
                                                try (PreparedStatement psChoice = conn.prepareStatement(sqlChoice)) {
                                                    for (String choice : q.getChoices()) {
                                                        psChoice.setInt(1, questionId);
                                                        psChoice.setString(2, choice);
                                                        psChoice.executeUpdate();
                                                    }
                                                } catch (SQLException ce) {
                                                    System.err.println("Choice insert error: " + ce.getMessage());
                                                }
                                            }
                                        }
                                    }
                                } catch (SQLException qe) {
                                    System.err.println("Question insert error: " + qe.getMessage());
                                }
                            }
                        }
                    } catch (SQLException che) {
                        System.err.println("Chapter insert error: " + che.getMessage());
                    }
                }
            }
            conn.commit();
            isSaved = true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException re) { System.err.println("Rollback error: " + re.getMessage()); }
            }
            throw new RuntimeException("Failed to save generated course: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ignore) {}
            }
        }
        return  isSaved;
    }


    public Boolean checkUserCourse(int id){

        boolean haveCourse = false;
        Connection conn = null;

        try {

            conn = DBConnection.getConnection();
            String sqlCourse = "SELECT * from generated_courses WHERE   user_id = ? ";
            PreparedStatement stmt = conn.prepareStatement(sqlCourse);

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if(rs.next()){
                haveCourse = true;
            }

            

        } catch(SQLException e){

            System.err.println("Error at checking user Course: " + e.getMessage());


        }

        return haveCourse;
    }
}
