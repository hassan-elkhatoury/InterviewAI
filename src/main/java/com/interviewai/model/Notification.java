package com.interviewai.model;

/**
 * In-app notification (e.g., reminders, achievements unlocked).
 * TODO: Types and scheduling via NotificationService.
 */
public class Notification {
    private int id;
    private int userId;
    private String type; // see enums.NotificationType
    private String message;
    private boolean read;
}
