package com.consumer.subserver.entity;

import java.util.Set;

public class Subscriber {
    private Set<String> topicList;
    private String subscriberIp;
    private String subscriberId;

    public Subscriber() {
    }

    public Subscriber(Set<String> topicList, String subscriberIp, String subscriberId) {
        this.topicList = topicList;
        this.subscriberIp = subscriberIp;
        this.subscriberId = subscriberId;
    }

    public Set<String> getTopicList() {
        return topicList;
    }

    public void setTopicList(Set<String> topicList) {
        this.topicList = topicList;
    }

    public String getSubscriberIp() {
        return subscriberIp;
    }

    public void setSubscriberIp(String subscriberIp) {
        this.subscriberIp = subscriberIp;
    }

    public String getSubscriberId() {
        return subscriberId;
    }

    public void setSubscriberId(String subscriberId) {
        this.subscriberId = subscriberId;
    }

    public void addTopic(String topicId) {
        this.topicList.add(topicId);
    }

    @Override
    public String toString() {
        return "Subscriber{" +
                "topicId=" + topicList +
                ", subscriberIp='" + subscriberIp + '\'' +
                ", subscriberId=" + subscriberId +
                '}';
    }
}
