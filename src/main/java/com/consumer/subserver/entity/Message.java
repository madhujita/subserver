package com.consumer.subserver.entity;

public class Message {
    private long topicId;
    private String message;

    public Message(long topicId, String message) {
        this.topicId = topicId;
        this.message = message;
    }

    public long getTopicId() {
        return topicId;
    }

    public void setTopicId(long topicId) {
        this.topicId = topicId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "Message{" +
                "topicId=" + topicId +
                ", message='" + message + '\'' +
                '}';
    }
}
