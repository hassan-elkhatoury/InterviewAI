package com.interviewai.model;

/**
 * Model class to store user onboarding preferences.
 */
public class OnboardingData {
    private int userId;
    private String interviewType; // JOB, VISA, INTERNSHIP, UNIVERSITY
    private String language; // ENGLISH, FRENCH, ARABIC, SPANISH
    private String timeline; // TOMORROW, THIS_WEEK, LATER
    private String context; // Industry/Program/Position
    private String cvPath; // Path to uploaded CV
    
    public OnboardingData() {}
    
    public OnboardingData(int userId, String interviewType, String language, String timeline, String context, String cvPath) {
        this.userId = userId;
        this.interviewType = interviewType;
        this.language = language;
        this.timeline = timeline;
        this.context = context;
        this.cvPath = cvPath;
    }
    
    // Getters and Setters
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public String getInterviewType() { return interviewType; }
    public void setInterviewType(String interviewType) { this.interviewType = interviewType; }
    
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    
    public String getTimeline() { return timeline; }
    public void setTimeline(String timeline) { this.timeline = timeline; }
    
    public String getContext() { return context; }
    public void setContext(String context) { this.context = context; }
    
    public String getCvPath() { return cvPath; }
    public void setCvPath(String cvPath) { this.cvPath = cvPath; }
    
    @Override
    public String toString() {
        return "OnboardingData{" +
                "userId=" + userId +
                ", interviewType='" + interviewType + '\'' +
                ", language='" + language + '\'' +
                ", timeline='" + timeline + '\'' +
                ", context='" + context + '\'' +
                ", cvPath='" + cvPath + '\'' +
                '}';
    }
}
