package com.interviewai.model;

import java.util.List;
import static java.util.Objects.requireNonNullElse;

public class Question {
    public enum QuestionType {
        MULTIPLE_CHOICE,
        SHORT_ANSWER
    }

    public static QuestionType typeFromString(String value) {
        if (value == null || value.isBlank()) {
            return QuestionType.MULTIPLE_CHOICE;
        }
        try {
            return QuestionType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return QuestionType.MULTIPLE_CHOICE;
        }
    }

    public enum QuestionStatus {
        NOT_STARTED,
        IN_PROGRESS,
        INCORRECT,
        COMPLETED;

        public static QuestionStatus fromString(String value) {
            if (value == null || value.isBlank()) {
                return NOT_STARTED;
            }
            try {
                return QuestionStatus.valueOf(value.trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                return NOT_STARTED;
            }
        }
    }
    
    private int id;
    private String question;
    private QuestionType questionType;
    private List<String> choices;
    private String correctAnswer;
    private String explanation;
    private QuestionStatus status;

    public Question() {
        this.questionType = QuestionType.MULTIPLE_CHOICE; // default
        this.status = QuestionStatus.NOT_STARTED;
    }

    public Question(int id, String question, QuestionType questionType, List<String> choices, String correctAnswer, String explanation) {
        this(id, question, questionType, choices, correctAnswer, explanation, QuestionStatus.NOT_STARTED);
    }

    public Question(int id, String question, QuestionType questionType, List<String> choices,
                    String correctAnswer, String explanation, QuestionStatus status) {
        this.id = id;
        this.question = question;
        this.questionType = requireNonNullElse(questionType, QuestionType.MULTIPLE_CHOICE);
        this.choices = choices;
        this.correctAnswer = correctAnswer;
        this.explanation = explanation;
        this.status = requireNonNullElse(status, QuestionStatus.NOT_STARTED);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public QuestionType getQuestionType() { return questionType; }
    public void setQuestionType(QuestionType questionType) { this.questionType = questionType; }

    public void setQuestionType(String questionType) {
        this.questionType = typeFromString(questionType);
    }

    public List<String> getChoices() { return choices; }
    public void setChoices(List<String> choices) { this.choices = choices; }

    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public QuestionStatus getStatus() { return status == null ? QuestionStatus.NOT_STARTED : status; }

    public void setStatus(QuestionStatus status) {
        this.status = requireNonNullElse(status, QuestionStatus.NOT_STARTED);
    }

    public void setStatus(String status) {
        this.status = QuestionStatus.fromString(status);
    }

    public String getStatusValue() {
        return getStatus().name();
    }

    @Override
    public String toString() {
        return "Question{" +
                "id=" + id +
                ", question='" + question + '\'' +
                ", questionType=" + questionType +
                ", choices=" + choices +
                ", correctAnswer='" + correctAnswer + '\'' +
                ", explanation='" + explanation + '\'' +
                ", status=" + getStatus() +
                '}';
    }
}
