package com.interviewai.model;

import java.util.List;

import static java.util.Objects.requireNonNullElse;

/**
 * Represents a chapter/module in a course.
 * Contains multiple questions for a specific topic.
 */
public class Chapter {
    public enum ChapterStatus {
        NOT_STARTED,
        IN_PROGRESS,
        COMPLETED;

        public static ChapterStatus fromString(String value) {
            if (value == null || value.isBlank()) {
                return NOT_STARTED;
            }
            try {
                return ChapterStatus.valueOf(value.trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                return NOT_STARTED;
            }
        }
    }

    private int id;
    private int chapterNumber;
    private String name;
    private String description;
    private List<Question> questions;
    private ChapterStatus status;
    private int totalQuestions;
    private int completedQuestions;
    
    // Constructors
    public Chapter() {
        this.status = ChapterStatus.NOT_STARTED;
    }
    
    public Chapter(int chapterNumber, String name, String description, List<Question> questions) {
        this(0, chapterNumber, name, description, questions, ChapterStatus.NOT_STARTED, 0, 0);
    }

    public Chapter(int id, int chapterNumber, String name, String description,
                   List<Question> questions, ChapterStatus status,
                   int totalQuestions, int completedQuestions) {
        this.id = id;
        this.chapterNumber = chapterNumber;
        this.name = name;
        this.description = description;
        this.questions = questions;
        this.status = requireNonNullElse(status, ChapterStatus.NOT_STARTED);
        this.totalQuestions = Math.max(0, totalQuestions);
        this.completedQuestions = Math.max(0, completedQuestions);
    }

    public Chapter(int id, int chapterNumber, String name, String description,
                   List<Question> questions, ChapterStatus status) {
        this(id, chapterNumber, name, description, questions, status, 0, 0);
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getChapterNumber() {
        return chapterNumber;
    }
    
    public void setChapterNumber(int chapterNumber) {
        this.chapterNumber = chapterNumber;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public List<Question> getQuestions() {
        return questions;
    }
    
    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }
    
    public int getQuestionCount() {
        return questions != null ? questions.size() : 0;
    }

    public ChapterStatus getStatus() {
        return status == null ? ChapterStatus.NOT_STARTED : status;
    }

    public void setStatus(ChapterStatus status) {
        this.status = requireNonNullElse(status, ChapterStatus.NOT_STARTED);
    }

    public void setStatus(String status) {
        this.status = ChapterStatus.fromString(status);
    }

    public String getStatusValue() {
        return getStatus().name();
    }

    public int getTotalQuestions() {
        if (totalQuestions == 0 && questions != null) {
            totalQuestions = questions.size();
        }
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = Math.max(0, totalQuestions);
    }

    public int getCompletedQuestions() {
        return Math.min(getTotalQuestions(), completedQuestions);
    }

    public void setCompletedQuestions(int completedQuestions) {
        this.completedQuestions = Math.max(0, completedQuestions);
    }

    public double getCompletionRatio() {
        int total = getTotalQuestions();
        return total == 0 ? 0.0 : (double) getCompletedQuestions() / total;
    }
    
    @Override
    public String toString() {
        return "Chapter{" +
                "id=" + id +
                ", chapterNumber=" + chapterNumber +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + getStatus() +
                ", completedQuestions=" + getCompletedQuestions() +
                ", totalQuestions=" + getTotalQuestions() +
                ", questionCount=" + getQuestionCount() +
                '}';
    }
}
