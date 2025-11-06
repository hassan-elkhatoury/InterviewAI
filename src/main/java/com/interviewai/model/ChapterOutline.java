package com.interviewai.model;

/**
 * Represents a chapter outline (without questions).
 * Used in the first stage of multi-stage course generation.
 */
public class ChapterOutline {
    private int chapterNumber;
    private String name;
    private String description;
    
    public ChapterOutline() {}
    
    public ChapterOutline(int chapterNumber, String name, String description) {
        this.chapterNumber = chapterNumber;
        this.name = name;
        this.description = description;
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
    
    @Override
    public String toString() {
        return "ChapterOutline{" +
                "chapterNumber=" + chapterNumber +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
