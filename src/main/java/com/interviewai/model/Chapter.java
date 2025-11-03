package com.interviewai.model;

import java.util.List;

/**
 * Represents a chapter/module in a course.
 * Contains multiple questions for a specific topic.
 */
public class Chapter {
    private int chapterNumber;
    private String name;
    private String description;
    private List<Question> questions;
    
    // Constructors
    public Chapter() {}
    
    public Chapter(int chapterNumber, String name, String description, List<Question> questions) {
        this.chapterNumber = chapterNumber;
        this.name = name;
        this.description = description;
        this.questions = questions;
    }
    
    // Getters and Setters
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
    
    @Override
    public String toString() {
        return "Chapter{" +
                "chapterNumber=" + chapterNumber +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", questionCount=" + getQuestionCount() +
                '}';
    }
}
