package com.interviewai.model;

import java.util.List;

public class Question {
    public enum QuestionType {
        MULTIPLE_CHOICE,
        SHORT_ANSWER
    }
    
    private int id;
    private String question;
    private QuestionType questionType;
    private List<String> choices;
    private String correctAnswer;
    private String explanation;

    public Question() {
        this.questionType = QuestionType.MULTIPLE_CHOICE; // default
    }

    public Question(int id, String question, QuestionType questionType, List<String> choices, String correctAnswer, String explanation) {
        this.id = id;
        this.question = question;
        this.questionType = questionType;
        this.choices = choices;
        this.correctAnswer = correctAnswer;
        this.explanation = explanation;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public QuestionType getQuestionType() { return questionType; }
    public void setQuestionType(QuestionType questionType) { this.questionType = questionType; }

    public List<String> getChoices() { return choices; }
    public void setChoices(List<String> choices) { this.choices = choices; }

    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    @Override
    public String toString() {
        return "Question{" +
                "id=" + id +
                ", question='" + question + '\'' +
                ", questionType=" + questionType +
                ", choices=" + choices +
                ", correctAnswer='" + correctAnswer + '\'' +
                ", explanation='" + explanation + '\'' +
                '}';
    }
}
