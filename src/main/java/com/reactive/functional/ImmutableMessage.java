package com.reactive.functional;

// Immutable message example
class ImmutableMessage {
    private final String content;
    private final long timestamp;

    public ImmutableMessage(String content) {
        this.content = content;
        this.timestamp = System.currentTimeMillis();
    }

    public String getContent() {
        return content;
    }

    public long getTimestamp() {
        return timestamp;
    }
}