package com.nidoham.kaveya.firebase.google.authentication;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class User implements Serializable {
    private String userId;              // Unique identifier
    private String username;            // User's username
    private String email;               // User's email
    private String fullName;            // User's full name
    private String profilePictureUrl;   // Profile picture URL
    private List<String> interests;     // User interests for personalization
    private String personalityPreference; // AI personality preference
    private String preferredLanguage;   // Language preference
    private boolean memoryEnabled;      // Memory of past interactions
    private boolean notificationsEnabled; // Whether notifications are enabled

    // Default Constructor
    public User() {
        this.userId = "";
        this.username = "";
        this.email = "";
        this.fullName = "";
        this.profilePictureUrl = "";
        this.interests = new ArrayList<>();
        this.personalityPreference = "empathetic";
        this.preferredLanguage = "en";
        this.memoryEnabled = true;
        this.notificationsEnabled = true;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public List<String> getInterests() {
        return interests;
    }

    public void setInterests(List<String> interests) {
        this.interests = interests;
    }

    public String getPersonalityPreference() {
        return personalityPreference;
    }

    public void setPersonalityPreference(String personalityPreference) {
        this.personalityPreference = personalityPreference;
    }

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public void setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    public boolean isMemoryEnabled() {
        return memoryEnabled;
    }

    public void setMemoryEnabled(boolean memoryEnabled) {
        this.memoryEnabled = memoryEnabled;
    }

    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }
}