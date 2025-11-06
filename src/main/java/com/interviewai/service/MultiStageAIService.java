package com.interviewai.service;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.interviewai.dao.CourseDAO;
import com.interviewai.model.Chapter;
import com.interviewai.model.ChapterOutline;
import com.interviewai.model.GeneratedCourse;
import com.interviewai.model.OnboardingData;
import com.interviewai.model.Question;

import javafx.application.Platform;

/**
 * Multi-stage AI course generation service.
 * 
 * Stage 1: Generate 12 chapter outlines
 * Stage 2: Generate 2 chapters at a time (6 requests) with 40 questions each (20 MC + 20 SA)
 */
public class MultiStageAIService {
    
    private final AIService aiService;
    private final CourseDAO courseDAO;
    
    public MultiStageAIService() {
        this.aiService = new AIService();
        this.courseDAO = new CourseDAO();
    }
    
    /**
     * Generate a complete course in multiple stages asynchronously.
     * Updates progress callback and calls completion callback when done.
     */
    public void generateCourseAsync(OnboardingData data, ProgressCallback progressCallback, Runnable onComplete) {
        new Thread(() -> {
            try {
                // Stage 1: Generate 12 chapter outlines
                updateProgress(progressCallback, "Generating course structure (12 chapters)...", 0);
                System.out.println("\n🤖 Stage 1: Generating chapter outlines...");
                
                String outlinesPrompt = aiService.buildChapterOutlinesPrompt(
                    data.getInterviewType(),
                    data.getLanguage(),
                    data.getTimeline(),
                    data.getContext()
                );
                
                String outlinesResponse = aiService.sendRequest(outlinesPrompt);
                List<ChapterOutline> outlines = aiService.parseChapterOutlines(outlinesResponse);
                
                if (outlines.isEmpty()) {
                    throw new RuntimeException("Failed to generate chapter outlines");
                }
                
                // Extract course title from the response
                String courseTitle = extractCourseTitle(outlinesResponse);
                System.out.println("✓ Generated " + outlines.size() + " chapter outlines for: " + courseTitle);
                
                // Stage 2: Generate chapters with questions (2 chapters at a time, 6 requests)
                List<Chapter> allChapters = new ArrayList<>();
                int totalRequests = 6;
                
                for (int i = 0; i < totalRequests; i++) {
                    int startChapter = (i * 2) + 1;
                    int endChapter = startChapter + 1;
                    int progressPercent = (int) ((i + 1) * 100.0 / (totalRequests + 1));
                    
                    updateProgress(progressCallback, 
                        String.format("Generating chapters %d-%d with questions...", startChapter, endChapter), 
                        progressPercent);
                    
                    System.out.println(String.format("\n🤖 Stage 2.%d: Generating chapters %d-%d...", 
                        (i+1), startChapter, endChapter));
                    
                    String chaptersPrompt = aiService.buildChapterQuestionsPrompt(
                        courseTitle,
                        outlines,
                        startChapter,
                        endChapter,
                        data.getLanguage()
                    );
                    
                    String chaptersResponse = aiService.sendRequest(chaptersPrompt);
                    List<Chapter> chapters = parseChaptersWithQuestions(chaptersResponse);
                    
                    if (chapters.isEmpty()) {
                        System.err.println("⚠️  Warning: No chapters generated for request " + (i+1));
                    } else {
                        allChapters.addAll(chapters);
                        System.out.println(String.format("✓ Generated %d chapters with %d total questions", 
                            chapters.size(), 
                            chapters.stream().mapToInt(Chapter::getQuestionCount).sum()));
                    }
                    
                    // Small delay between requests to avoid rate limiting
                    Thread.sleep(1000);
                }
                
                // Create and save the complete course
                updateProgress(progressCallback, "Saving course to database...", 95);
                System.out.println("\n💾 Saving complete course to database...");
                
                GeneratedCourse course = new GeneratedCourse(
                    data.getUserId(),
                    courseTitle,
                    data.getInterviewType(),
                    data.getLanguage(),
                    allChapters
                );
                
                courseDAO.saveGeneratedCourse(course);
                
                updateProgress(progressCallback, "Course generation complete!", 100);
                System.out.println(String.format("✅ Course saved successfully: %d chapters, %d total questions", 
                    allChapters.size(), 
                    allChapters.stream().mapToInt(Chapter::getQuestionCount).sum()));
                
            } catch (Exception e) {
                System.err.println("❌ Course generation failed: " + e.getMessage());
                e.printStackTrace();
                updateProgress(progressCallback, "Error: " + e.getMessage(), -1);
            } finally {
                // Always call completion callback
                if (onComplete != null) {
                    Platform.runLater(onComplete);
                }
            }
        }).start();
    }
    
    /**
     * Extract course title from the outlines response.
     */
    private String extractCourseTitle(String jsonResponse) {
        try {
            JSONObject root = new JSONObject(jsonResponse);
            
            // Try to extract from Gemini's response structure
            if (root.has("candidates")) {
                JSONArray candidates = root.getJSONArray("candidates");
                if (candidates.length() > 0) {
                    JSONObject firstCandidate = candidates.getJSONObject(0);
                    if (firstCandidate.has("content")) {
                        JSONObject content = firstCandidate.getJSONObject("content");
                        if (content.has("parts")) {
                            JSONArray parts = content.getJSONArray("parts");
                            if (parts.length() > 0) {
                                String text = parts.getJSONObject(0).getString("text");
                                text = text.replaceAll("```json\\s*", "").replaceAll("```\\s*$", "").trim();
                                JSONObject courseData = new JSONObject(text);
                                return courseData.optString("course_title", "Interview Preparation Course");
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error extracting course title: " + e.getMessage());
        }
        return "Interview Preparation Course";
    }
    
    /**
     * Parse chapters with questions from AI response.
     */
    private List<Chapter> parseChaptersWithQuestions(String jsonResponse) {
        List<Chapter> chapters = new ArrayList<>();
        try {
            JSONObject root = new JSONObject(jsonResponse);
            
            // Extract from Gemini's response structure
            String chaptersJson = jsonResponse;
            if (root.has("candidates")) {
                JSONArray candidates = root.getJSONArray("candidates");
                if (candidates.length() > 0) {
                    JSONObject firstCandidate = candidates.getJSONObject(0);
                    if (firstCandidate.has("content")) {
                        JSONObject content = firstCandidate.getJSONObject("content");
                        if (content.has("parts")) {
                            JSONArray parts = content.getJSONArray("parts");
                            if (parts.length() > 0) {
                                chaptersJson = parts.getJSONObject(0).getString("text");
                                chaptersJson = chaptersJson.replaceAll("```json\\s*", "").replaceAll("```\\s*$", "").trim();
                            }
                        }
                    }
                }
            }
            
            JSONObject chaptersData = new JSONObject(chaptersJson);
            JSONArray chaptersArray = chaptersData.getJSONArray("chapters");
            
            for (int i = 0; i < chaptersArray.length(); i++) {
                JSONObject chapterJson = chaptersArray.getJSONObject(i);
                
                int chapterNumber = chapterJson.getInt("chapter_number");
                String name = chapterJson.getString("name");
                String description = chapterJson.getString("description");
                
                List<Question> questions = new ArrayList<>();
                JSONArray questionsArray = chapterJson.getJSONArray("questions");
                
                for (int j = 0; j < questionsArray.length(); j++) {
                    JSONObject qJson = questionsArray.getJSONObject(j);
                    
                    Question question = new Question();
                    question.setId(qJson.optInt("id", j + 1));
                    question.setQuestion(qJson.getString("question"));
                    
                    // Determine question type
                    String typeStr = qJson.optString("question_type", "MULTIPLE_CHOICE");
                    question.setQuestionType(
                        typeStr.equals("SHORT_ANSWER") ? 
                        Question.QuestionType.SHORT_ANSWER : 
                        Question.QuestionType.MULTIPLE_CHOICE
                    );
                    
                    // Parse choices (only for multiple choice)
                    if (question.getQuestionType() == Question.QuestionType.MULTIPLE_CHOICE) {
                        List<String> choices = new ArrayList<>();
                        JSONArray choicesArray = qJson.optJSONArray("choices");
                        if (choicesArray != null) {
                            for (int k = 0; k < choicesArray.length(); k++) {
                                choices.add(choicesArray.getString(k));
                            }
                        }
                        question.setChoices(choices);
                    }
                    
                    question.setCorrectAnswer(qJson.getString("correct_answer"));
                    question.setExplanation(qJson.optString("explanation", ""));
                    
                    questions.add(question);
                }
                
                Chapter chapter = new Chapter(chapterNumber, name, description, questions);
                chapters.add(chapter);
            }
            
        } catch (Exception e) {
            System.err.println("Error parsing chapters with questions: " + e.getMessage());
            e.printStackTrace();
        }
        return chapters;
    }
    
    /**
     * Update progress on the JavaFX UI thread.
     */
    private void updateProgress(ProgressCallback callback, String message, int percent) {
        if (callback != null) {
            Platform.runLater(() -> callback.onProgress(message, percent));
        }
    }
    
    /**
     * Callback interface for progress updates.
     */
    public interface ProgressCallback {
        void onProgress(String message, int percent);
    }
}
