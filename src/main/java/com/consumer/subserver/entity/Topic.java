package com.consumer.subserver.entity;

public class Topic {
    private String topicId;
    private String subscriberId;

    public Topic(String topicId, String subscriberId) {
        this.topicId = topicId;
        this.subscriberId = subscriberId;
    }

    public String getTopicId() {
        return topicId;
    }

    public void setTopicId(String topicId) {
        this.topicId = topicId;
    }

    public String getSubscriberId() {
        return subscriberId;
    }

    public void setSubscriberId(String subscriberId) {
        this.subscriberId = subscriberId;
    }

    @Override
    public String toString() {
        return "Topic{" +
                "topicId='" + topicId + '\'' +
                ", subscriberId='" + subscriberId + '\'' +
                '}';
    }
}
