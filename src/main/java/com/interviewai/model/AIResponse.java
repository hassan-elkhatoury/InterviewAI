package com.interviewai.model;

/**
 * Generic structure for AI responses (text, metadata, tokens).
 * TODO: Map provider-specific fields (OpenAI, etc.).
 */
public class AIResponse {
    private String text;
    private String model;
    private int promptTokens;
    private int completionTokens;
}
