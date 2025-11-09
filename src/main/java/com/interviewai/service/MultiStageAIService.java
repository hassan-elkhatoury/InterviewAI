package com.interviewai.service;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.interviewai.dao.CourseDAO;
import com.interviewai.dao.InterviewPrepDAO;
import com.interviewai.model.Chapter;
import com.interviewai.model.ChapterOutline;
import com.interviewai.model.GeneratedCourse;
import com.interviewai.model.OnboardingData;
import com.interviewai.model.Question;

import javafx.application.Platform;

/**
 * Multi-stage AI course generation service.
 * 
 * Generates both technical and soft-skills courses for comprehensive interview prep.
 * Stage 1: Generate 12 chapter outlines
 * Stage 2: Generate 2 chapters at a time (6 requests) with 40 questions each (20 MC + 20 SA)
 */
public class MultiStageAIService {
    
    private final AIService aiService;
    private final CourseDAO courseDAO;
    private final InterviewPrepDAO interviewPrepDAO;
    
    public MultiStageAIService() {
        this.aiService = new AIService();
        this.courseDAO = new CourseDAO();
        this.interviewPrepDAO = new InterviewPrepDAO();
    }
    
    /**
     * Generate both technical and soft-skills courses asynchronously.
     * Progress: 0-45% technical course, 50-95% soft-skills course, 95-100% saving to DB.
     */
    public void generateCourseAsync(OnboardingData data, ProgressCallback progressCallback, Runnable onComplete) {
        new Thread(() -> {
            try {
                // Generate technical course (0-45%)
                updateProgress(progressCallback, "Starting technical course generation...", 0);
                System.out.println("\n🎯 Generating TECHNICAL course...");
                int technicalCourseId = generateSingleCourse(data, true, progressCallback, 0, 45);
                
                if (technicalCourseId == -1) {
                    throw new RuntimeException("Failed to generate technical course");
                }
                
                // Generate soft-skills course (50-95%)
                updateProgress(progressCallback, "Starting soft-skills course generation...", 50);
                System.out.println("\n🎯 Generating SOFT-SKILLS course...");
                int softskillsCourseId = generateSingleCourse(data, false, progressCallback, 50, 95);
                
                if (softskillsCourseId == -1) {
                    throw new RuntimeException("Failed to generate soft-skills course");
                }
                
                // Save to interview_prep table
                updateProgress(progressCallback, "Linking courses to interview prep...", 97);
                System.out.println("\n💾 Saving interview prep record...");
                boolean saved = interviewPrepDAO.insertInterviewPrep(data.getUserId(), technicalCourseId, softskillsCourseId);
                
                if (!saved) {
                    System.err.println("⚠️  Warning: Failed to save interview prep record");
                }
                
                updateProgress(progressCallback, "Interview preparation courses ready!", 100);
                System.out.println("✅ Both courses generated successfully!");
                
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
     * Generate a single course (technical or soft-skills) in multiple stages.
     * 
     * @param data Onboarding data
     * @param isTechnical true for technical course, false for soft-skills
     * @param progressCallback Progress callback
     * @param progressStart Starting progress percentage (0-100)
     * @param progressEnd Ending progress percentage (0-100)
     * @return The generated course ID, or -1 if failed
     */
    private int generateSingleCourse(OnboardingData data, boolean isTechnical, 
                                     ProgressCallback progressCallback, 
                                     int progressStart, int progressEnd) {
        try {
            // Adjust context for soft-skills course
            String context = data.getContext();
            String courseType = isTechnical ? "TECHNICAL" : "SOFT_SKILLS";
            
            if (!isTechnical) {
                // For soft-skills, modify the context to focus on non-technical aspects
                context = "Soft-skills and behavioral interview preparation for " + data.getContext();
            }
            
            // Stage 1: Generate 12 chapter outlines
            int outlineProgress = progressStart + ((progressEnd - progressStart) / 10);
            updateProgress(progressCallback, 
                String.format("Generating %s course structure (12 chapters)...", 
                    isTechnical ? "technical" : "soft-skills"), 
                outlineProgress);
            
            System.out.println(String.format("\n🤖 Stage 1 (%s): Generating chapter outlines...", courseType));
            
            String outlinesPrompt = aiService.buildChapterOutlinesPrompt(
                data.getInterviewType(),
                data.getLanguage(),
                data.getTimeline(),
                context
            );
            
            String outlinesResponse = aiService.sendRequestWithProgress(
                outlinesPrompt,
                progressCallback,
                String.format("Stage 1 (%s outlines)", courseType),
                outlineProgress
            );
            List<ChapterOutline> outlines = aiService.parseChapterOutlines(outlinesResponse);
            
            if (outlines.isEmpty()) {
                throw new RuntimeException("Failed to generate chapter outlines for " + courseType);
            }
            
            // Extract course title from the response
            String courseTitle = extractCourseTitle(outlinesResponse);
            if (!isTechnical) {
                courseTitle = "Soft-Skills: " + courseTitle;
            }
            System.out.println(String.format("✓ Generated %d chapter outlines for: %s", outlines.size(), courseTitle));
            
            // Stage 2: Generate chapters with questions (2 chapters at a time, 6 requests)
            List<Chapter> allChapters = new ArrayList<>();
            int totalRequests = 6;
            int progressRange = progressEnd - progressStart;
            
            for (int i = 0; i < totalRequests; i++) {
                int startChapter = (i * 2) + 1;
                int endChapter = startChapter + 1;
                
                // Calculate progress proportionally within the range
                int chapterProgress = progressStart + ((i + 2) * progressRange / (totalRequests + 2));
                
                updateProgress(progressCallback, 
                    String.format("Generating %s chapters %d-%d with questions...", 
                        isTechnical ? "technical" : "soft-skills",
                        startChapter, endChapter), 
                    chapterProgress);
                
                System.out.println(String.format("\n🤖 Stage 2.%d (%s): Generating chapters %d-%d...", 
                    (i+1), courseType, startChapter, endChapter));
                
                String chaptersPrompt = aiService.buildChapterQuestionsPrompt(
                    courseTitle,
                    outlines,
                    startChapter,
                    endChapter,
                    data.getLanguage()
                );
                
                String chaptersResponse = aiService.sendRequestWithProgress(
                    chaptersPrompt,
                    progressCallback,
                    String.format("Stage 2.%d (%s chapters %d-%d)", (i+1), courseType, startChapter, endChapter),
                    chapterProgress
                );
                List<Chapter> chapters = parseChaptersWithQuestions(chaptersResponse);
                
                if (chapters.isEmpty()) {
                    System.err.println(String.format("⚠️  Warning: No chapters generated for %s request %d", courseType, (i+1)));
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
            int saveProgress = progressStart + ((progressRange * 9) / 10);
            updateProgress(progressCallback, 
                String.format("Saving %s course to database...", isTechnical ? "technical" : "soft-skills"), 
                saveProgress);
            
            System.out.println(String.format("\n💾 Saving complete %s course to database...", courseType));
            
            GeneratedCourse course = new GeneratedCourse(
                data.getUserId(),
                courseTitle,
                data.getInterviewType(),
                data.getLanguage(),
                allChapters
            );
            
            int courseId = courseDAO.saveGeneratedCourse(course);
            
            updateProgress(progressCallback, 
                String.format("%s course generation complete!", isTechnical ? "Technical" : "Soft-skills"), 
                progressEnd);
            
            System.out.println(String.format("✅ %s course saved successfully (ID: %d): %d chapters, %d total questions", 
                courseType, courseId,
                allChapters.size(), 
                allChapters.stream().mapToInt(Chapter::getQuestionCount).sum()));
            
            return courseId;
            
        } catch (Exception e) {
            System.err.println(String.format("❌ %s course generation failed: %s", 
                isTechnical ? "Technical" : "Soft-skills", e.getMessage()));
            e.printStackTrace();
            return -1;
        }
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
            
            // First attempt direct parse
            JSONObject chaptersData;
            try {
                chaptersData = new JSONObject(chaptersJson);
            } catch (Exception primaryParseEx) {
                System.err.println("Primary JSON parse failed (" + primaryParseEx.getMessage() + "). Applying sanitization...");
                chaptersJson = sanitizeChaptersJson(chaptersJson);
                try {
                    chaptersData = new JSONObject(chaptersJson);
                } catch (Exception secondaryParseEx) {
                    System.err.println("Sanitized JSON parse still failed: " + secondaryParseEx.getMessage());
                    // Give up gracefully
                    return chapters;
                }
            }
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
     * Attempt to clean AI produced JSON so org.json can parse it.
     * Handles:
     *  - Duplicate keys like "explanation" inside the same object
     *  - Missing commas between sibling objects ("}{" -> "},{")
     *  - Raw array without root object (wraps into {"chapters": [...]})
     *  - Stray markdown fences / code block markers
     */
    private String sanitizeChaptersJson(String raw) {
        if (raw == null) return "{\"chapters\":[]}";
        String cleaned = raw.trim();
        // Remove markdown fences
        cleaned = cleaned.replaceAll("```json", "").replaceAll("```", "").trim();
        // Wrap if it's just an array
        if (cleaned.startsWith("[") && cleaned.endsWith("]")) {
            cleaned = "{\"chapters\":" + cleaned + "}";
        }
        // Fix missing commas between objects in arrays: }{ -> },{
        cleaned = cleaned.replaceAll("}\\s*{", "},{");
        // Remove duplicate "explanation" keys within the same object keeping first occurrence.
        // Strategy: For each object block, track explanation occurrences.
        StringBuilder sb = new StringBuilder();
        int braceDepth = 0;
        boolean inString = false;
        boolean escape = false;
        int objStart = -1;
        List<Integer> explanationPositions = new ArrayList<>();
        for (int i = 0; i < cleaned.length(); i++) {
            char c = cleaned.charAt(i);
            sb.append(c);
            if (escape) { escape = false; continue; }
            if (c == '\\') { escape = true; continue; }
            if (c == '"') { inString = !inString; }
            if (!inString) {
                if (c == '{') {
                    if (braceDepth == 0) {
                        objStart = sb.length() - 1;
                        explanationPositions.clear();
                    }
                    braceDepth++;
                } else if (c == '}') {
                    braceDepth--;
                    if (braceDepth == 0 && explanationPositions.size() > 1) {
                        // Post-process this object substring to remove duplicate explanation keys beyond first
                        int objEnd = sb.length();
                        String obj = sb.substring(objStart, objEnd);
                        // Remove subsequent explanation keys with a simplistic regex
                        // Matches ,"explanation":"..." following the first
                        String firstPattern = "\\\\\"explanation\\\\\":"; // marker
                        int firstIdx = obj.indexOf("\"explanation\":");
                        if (firstIdx >= 0) {
                            // keep first, remove later occurrences
                            String before = obj.substring(0, firstIdx + "\"explanation\":".length());
                            String after = obj.substring(firstIdx + "\"explanation\":".length());
                            // Find subsequent occurrences and remove preceding comma segment
                            // Regex replace: ,"explanation":"..."
                            after = after.replaceAll(",\\s*\\\"explanation\\\":\\s*\\\"[^\\\"]*\\\"", "");
                            obj = before + after;
                            // Rebuild sb
                            sb.setLength(objStart);
                            sb.append(obj);
                        }
                    }
                }
            }
        }
        cleaned = sb.toString();
        // Basic sanity: ensure it starts with '{'
        if (!cleaned.trim().startsWith("{")) {
            cleaned = "{\"chapters\":[]}"; // fallback
        }
        return cleaned;
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
