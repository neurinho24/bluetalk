package com.bluetalk.model;

public class ChatMessage {
    public enum Type { TEXT, IMAGE, AUDIO }

    private final String sender;
    private final String content;
    private final long timestamp;
    private final Type type;
    private final boolean localUser;

    public ChatMessage(String sender, String content, long timestamp, Type type, boolean localUser) {
        this.sender = sender;
        this.content = content;
        this.timestamp = timestamp;
        this.type = type;
        this.localUser = localUser;
    }

    public String getSender() { return sender; }
    public String getContent() { return content; }
    public long getTimestamp() { return timestamp; }
    public Type getType() { return type; }
    public boolean isLocalUser() { return localUser; }
}
