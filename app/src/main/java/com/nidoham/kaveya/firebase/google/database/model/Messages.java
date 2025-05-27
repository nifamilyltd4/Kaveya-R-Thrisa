package com.nidoham.kaveya.firebase.google.database.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Enhanced model class representing a chat message with 2025 technology features.
 * Created by: nifamilyltd4
 * Last updated: 2025-04-21
 */
public final class Messages {
    
    public enum MediaType {
        TEXT, IMAGE, VIDEO, VOICE, FILE, LOCATION, CONTACT, STICKER
    }
    
    private String id;
    private String userId;
    private String characterId;
    private String text;
    private long timestamp;
    private String mediaUrl;
    private MediaType mediaType;
    private boolean isAI;
    private Map<String, Object> metadata;
    private boolean isTranslated;
    private String originalLanguage;
    private String translatedLanguage;
    private boolean isEdited;
    private long editTimestamp;
    private boolean isForwarded;
    private String forwardedFrom;
    private boolean isReplying;
    private String replyToMessageId;
    private boolean isDeleted;
    
    /** Default constructor for Firebase */
    public Messages() {
        this.id = null;
        this.userId = null;
        this.characterId = null;
        this.text = null;
        this.timestamp = 0L;
        this.mediaUrl = null;
        this.mediaType = MediaType.TEXT;
        this.isAI = false;
        this.metadata = new HashMap<>();
        this.isTranslated = false;
        this.originalLanguage = null;
        this.translatedLanguage = null;
        this.isEdited = false;
        this.editTimestamp = 0L;
        this.isForwarded = false;
        this.forwardedFrom = null;
        this.isReplying = false;
        this.replyToMessageId = null;
        this.isDeleted = false;
    }
    
    /** Constructor for text message */
    public Messages(String userId, String characterId, String text, boolean isAI) {
        this.userId = Objects.requireNonNull(userId, "userId cannot be null");
        this.characterId = Objects.requireNonNull(characterId, "characterId cannot be null");
        this.text = Objects.requireNonNull(text, "text cannot be null");
        this.isAI = isAI;
        this.timestamp = System.currentTimeMillis();
        this.mediaType = MediaType.TEXT;
        this.id = generateId();
        this.mediaUrl = null;
        this.metadata = new HashMap<>();
        this.isTranslated = false;
        this.originalLanguage = null;
        this.translatedLanguage = null;
        this.isEdited = false;
        this.editTimestamp = 0L;
        this.isForwarded = false;
        this.forwardedFrom = null;
        this.isReplying = false;
        this.replyToMessageId = null;
        this.isDeleted = false;
    }
    
    /** Constructor for media message */
    public Messages(String userId, String characterId, String text, 
                    String mediaUrl, MediaType mediaType, boolean isAI) {
        this.userId = Objects.requireNonNull(userId, "userId cannot be null");
        this.characterId = Objects.requireNonNull(characterId, "characterId cannot be null");
        this.text = Objects.requireNonNull(text, "text cannot be null");
        this.isAI = isAI;
        this.timestamp = System.currentTimeMillis();
        this.mediaUrl = Objects.requireNonNull(mediaUrl, "mediaUrl cannot be null");
        this.mediaType = Objects.requireNonNull(mediaType, "mediaType cannot be null");
        this.id = generateId();
        this.metadata = new HashMap<>();
        this.isTranslated = false;
        this.originalLanguage = null;
        this.translatedLanguage = null;
        this.isEdited = false;
        this.editTimestamp = 0L;
        this.isForwarded = false;
        this.forwardedFrom = null;
        this.isReplying = false;
        this.replyToMessageId = null;
        this.isDeleted = false;
    }
    
    /** Full constructor with all fields */
    private Messages(Builder builder) {
        this.id = builder.id != null ? builder.id : generateId();
        this.userId = Objects.requireNonNull(builder.userId, "userId cannot be null");
        this.characterId = Objects.requireNonNull(builder.characterId, "characterId cannot be null");
        this.text = Objects.requireNonNull(builder.text, "text cannot be null");
        this.isAI = builder.isAI;
        this.timestamp = builder.timestamp > 0 ? builder.timestamp : System.currentTimeMillis();
        this.mediaUrl = builder.mediaUrl;
        this.mediaType = builder.mediaType != null ? builder.mediaType : MediaType.TEXT;
        this.metadata = builder.metadata != null ? builder.metadata : new HashMap<>();
        this.isTranslated = builder.isTranslated;
        this.originalLanguage = builder.originalLanguage;
        this.translatedLanguage = builder.translatedLanguage;
        this.isEdited = builder.isEdited;
        this.editTimestamp = builder.editTimestamp;
        this.isForwarded = builder.isForwarded;
        this.forwardedFrom = builder.forwardedFrom;
        this.isReplying = builder.isReplying;
        this.replyToMessageId = builder.replyToMessageId;
        this.isDeleted = builder.isDeleted;
    }
    
    /**
     * Builder pattern for creating Messages with many optional parameters
     */
    public static class Builder {
        private String id;
        private String userId;
        private String characterId;
        private String text;
        private long timestamp;
        private String mediaUrl;
        private MediaType mediaType;
        private boolean isAI;
        private Map<String, Object> metadata;
        private boolean isTranslated;
        private String originalLanguage;
        private String translatedLanguage;
        private boolean isEdited;
        private long editTimestamp;
        private boolean isForwarded;
        private String forwardedFrom;
        private boolean isReplying;
        private String replyToMessageId;
        private boolean isDeleted;
        
        public Builder(String userId, String characterId, String text) {
            this.userId = userId;
            this.characterId = characterId;
            this.text = text;
            this.metadata = new HashMap<>();
        }
        
        public Builder id(String id) {
            this.id = id;
            return this;
        }
        
        public Builder isAI(boolean isAI) {
            this.isAI = isAI;
            return this;
        }
        
        public Builder timestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        public Builder mediaUrl(String mediaUrl) {
            this.mediaUrl = mediaUrl;
            return this;
        }
        
        public Builder mediaType(MediaType mediaType) {
            this.mediaType = mediaType;
            return this;
        }
        
        public Builder addMetadata(String key, Object value) {
            if (this.metadata == null) {
                this.metadata = new HashMap<>();
            }
            this.metadata.put(key, value);
            return this;
        }
        
        public Builder isTranslated(boolean isTranslated) {
            this.isTranslated = isTranslated;
            return this;
        }
        
        public Builder originalLanguage(String originalLanguage) {
            this.originalLanguage = originalLanguage;
            return this;
        }
        
        public Builder translatedLanguage(String translatedLanguage) {
            this.translatedLanguage = translatedLanguage;
            return this;
        }
        
        public Builder isEdited(boolean isEdited) {
            this.isEdited = isEdited;
            return this;
        }
        
        public Builder editTimestamp(long editTimestamp) {
            this.editTimestamp = editTimestamp;
            return this;
        }
        
        public Builder isForwarded(boolean isForwarded) {
            this.isForwarded = isForwarded;
            return this;
        }
        
        public Builder forwardedFrom(String forwardedFrom) {
            this.forwardedFrom = forwardedFrom;
            return this;
        }
        
        public Builder isReplying(boolean isReplying) {
            this.isReplying = isReplying;
            return this;
        }
        
        public Builder replyToMessageId(String replyToMessageId) {
            this.replyToMessageId = replyToMessageId;
            return this;
        }
        
        public Builder isDeleted(boolean isDeleted) {
            this.isDeleted = isDeleted;
            return this;
        }
        
        public Messages build() {
            return new Messages(this);
        }
    }
    
    /**
     * Generate a unique ID for the message
     */
    private String generateId() {
        return userId + "_" + characterId + "_" + System.currentTimeMillis() + "_" + 
               Math.round(Math.random() * 1000000);
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getCharacterId() { return characterId; }
    public void setCharacterId(String characterId) { this.characterId = characterId; }
    
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    
    public String getMediaUrl() { return mediaUrl; }
    public void setMediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; }
    
    public MediaType getMediaType() { return mediaType; }
    public void setMediaType(MediaType mediaType) { this.mediaType = mediaType; }
    
    public boolean isAI() { return isAI; }
    public void setAI(boolean isAI) { this.isAI = isAI; }
    
    public Map<String, Object> getMetadata() { return new HashMap<>(metadata); }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    
    public Object getMetadata(String key) { return metadata.get(key); }
    public void addMetadata(String key, Object value) { this.metadata.put(key, value); }
    
    public boolean isTranslated() { return isTranslated; }
    public void setTranslated(boolean isTranslated) { this.isTranslated = isTranslated; }
    
    public String getOriginalLanguage() { return originalLanguage; }
    public void setOriginalLanguage(String originalLanguage) { this.originalLanguage = originalLanguage; }
    
    public String getTranslatedLanguage() { return translatedLanguage; }
    public void setTranslatedLanguage(String translatedLanguage) { this.translatedLanguage = translatedLanguage; }
    
    public boolean isEdited() { return isEdited; }
    public void setEdited(boolean isEdited) { this.isEdited = isEdited; }
    
    public long getEditTimestamp() { return editTimestamp; }
    public void setEditTimestamp(long editTimestamp) { this.editTimestamp = editTimestamp; }
    
    public boolean isForwarded() { return isForwarded; }
    public void setForwarded(boolean isForwarded) { this.isForwarded = isForwarded; }
    
    public String getForwardedFrom() { return forwardedFrom; }
    public void setForwardedFrom(String forwardedFrom) { this.forwardedFrom = forwardedFrom; }
    
    public boolean isReplying() { return isReplying; }
    public void setReplying(boolean isReplying) { this.isReplying = isReplying; }
    
    public String getReplyToMessageId() { return replyToMessageId; }
    public void setReplyToMessageId(String replyToMessageId) { this.replyToMessageId = replyToMessageId; }
    
    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean isDeleted) { this.isDeleted = isDeleted; }
    
    public boolean hasMedia() {
        return mediaUrl != null && !mediaUrl.isEmpty();
    }
    
    /**
     * Create a translated version of this message
     */
    public Messages withTranslation(String translatedText, String originalLang, String targetLang) {
        return new Builder(userId, characterId, translatedText)
                .id(id)
                .isAI(isAI)
                .timestamp(timestamp)
                .mediaUrl(mediaUrl)
                .mediaType(mediaType)
                .isTranslated(true)
                .originalLanguage(originalLang)
                .translatedLanguage(targetLang)
                .build();
    }
    
    /**
     * Create an edited version of this message
     */
    public Messages withEdit(String newText) {
        return new Builder(userId, characterId, newText)
                .id(id)
                .isAI(isAI)
                .timestamp(timestamp)
                .mediaUrl(mediaUrl)
                .mediaType(mediaType)
                .isEdited(true)
                .editTimestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * Create a reply to this message
     */
    public Messages createReply(String replyText, boolean fromAI) {
        return new Builder(userId, characterId, replyText)
                .isAI(fromAI)
                .isReplying(true)
                .replyToMessageId(id)
                .build();
    }
    
    /**
     * Create a forwarded version of this message
     */
    public Messages createForwarded(String newUserId, String newCharacterId) {
        return new Builder(newUserId, newCharacterId, text)
                .isAI(isAI)
                .mediaUrl(mediaUrl)
                .mediaType(mediaType)
                .isForwarded(true)
                .forwardedFrom(userId)
                .build();
    }
    
    /**
     * Create a deleted version of this message
     */
    public Messages asDeleted() {
        return new Builder(userId, characterId, "This message was deleted")
                .id(id)
                .isAI(isAI)
                .timestamp(timestamp)
                .isDeleted(true)
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Messages messages = (Messages) o;
        return timestamp == messages.timestamp &&
               isAI == messages.isAI &&
               isTranslated == messages.isTranslated &&
               isEdited == messages.isEdited &&
               editTimestamp == messages.editTimestamp &&
               isForwarded == messages.isForwarded &&
               isReplying == messages.isReplying &&
               isDeleted == messages.isDeleted &&
               Objects.equals(id, messages.id) &&
               Objects.equals(userId, messages.userId) &&
               Objects.equals(characterId, messages.characterId) &&
               Objects.equals(text, messages.text) &&
               Objects.equals(mediaUrl, messages.mediaUrl) &&
               mediaType == messages.mediaType &&
               Objects.equals(metadata, messages.metadata) &&
               Objects.equals(originalLanguage, messages.originalLanguage) &&
               Objects.equals(translatedLanguage, messages.translatedLanguage) &&
               Objects.equals(forwardedFrom, messages.forwardedFrom) &&
               Objects.equals(replyToMessageId, messages.replyToMessageId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, userId, characterId, text, timestamp, mediaUrl, mediaType, isAI,
                metadata, isTranslated, originalLanguage, translatedLanguage, isEdited, 
                editTimestamp, isForwarded, forwardedFrom, isReplying, replyToMessageId, isDeleted);
    }
    
    @Override
    public String toString() {
        return "Messages{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", characterId='" + characterId + '\'' +
                ", text='" + text + '\'' +
                ", timestamp=" + timestamp +
                ", mediaType=" + mediaType +
                (mediaUrl != null ? ", mediaUrl='" + mediaUrl + '\'' : "") +
                ", isAI=" + isAI +
                (isTranslated ? ", translated=true" : "") +
                (isEdited ? ", edited=true" : "") +
                (isForwarded ? ", forwarded=true" : "") +
                (isReplying ? ", replying=true" : "") +
                (isDeleted ? ", deleted=true" : "") +
                '}';
    }
}