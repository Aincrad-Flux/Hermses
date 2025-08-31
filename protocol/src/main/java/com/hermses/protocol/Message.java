package com.hermses.protocol;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public class Message {
    private final MessageType type;
    private final String sender;
    private final String content;
    private final long timestamp;

    @JsonCreator
    public Message(@JsonProperty("type") MessageType type,
                   @JsonProperty("sender") String sender,
                   @JsonProperty("content") String content,
                   @JsonProperty("timestamp") Long timestamp) {
        this.type = type;
        this.sender = sender;
        this.content = content;
        this.timestamp = timestamp != null ? timestamp : Instant.now().toEpochMilli();
    }

    public static Message chat(String sender, String content) {
        return new Message(MessageType.CHAT, sender, content, Instant.now().toEpochMilli());
    }

    public static Message users(String csv) {
        return new Message(MessageType.USERS, "server", csv, Instant.now().toEpochMilli());
    }

    public MessageType getType() { return type; }
    public String getSender() { return sender; }
    public String getContent() { return content; }
    public long getTimestamp() { return timestamp; }
}
