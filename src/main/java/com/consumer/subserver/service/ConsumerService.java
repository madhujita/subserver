package com.consumer.subserver.service;

import com.consumer.subserver.entity.Message;
import com.consumer.subserver.entity.MessageList;
import com.consumer.subserver.entity.Subscriber;
import com.consumer.subserver.entity.Topic;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
public class ConsumerService {

    static private String queueUrl;
    static private RestTemplate restTemplate;
    static private HashMap<Long, MessageList> map;
    static private List<Subscriber> subscriberList;
    static private Map<String, Set<String>> subscriberTopicMap;
    private ReentrantReadWriteLock reentrantReadWriteLock;
    ObjectMapper mapper;
    UUID uuid;

    //TODO - remove
    private MessageList dummyMsgList = new MessageList();

    public ConsumerService() {
        queueUrl = "";
        restTemplate = new RestTemplate();
        map = new HashMap<>();
        reentrantReadWriteLock = new ReentrantReadWriteLock();
        mapper = new ObjectMapper();
        subscriberList = new ArrayList<>();
        subscriberTopicMap = new HashMap<>();
    }

    //TODO - remove after testing if http call goes through
    public void test() {
        String url = "http://localhost:8081/subscriber/health";
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
//        headers.add("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
//                "(KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");
        HttpEntity<String> entity = new HttpEntity<>("parameters", headers);
        ResponseEntity<String> result =
                restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        System.out.println("success api");
        System.out.println(result.getBody());
    }

    @Async
    public void fetchMessage() throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
//        headers.add("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
//                "(KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");
        HttpEntity<String> entity = new HttpEntity<>("parameters", headers);
        while (true) {
            ResponseEntity<Message> result =
                    restTemplate.exchange(queueUrl, HttpMethod.GET, entity, Message.class);
            if (result.getStatusCode().is4xxClientError()) {
                return;
            }
            Message message = result.getBody();
            addEntry(message);
            sendMessagesToSubscribers();
        }
//        populateDummyMsgsFromQ();
//        for (Message m : dummyMsgList.getMessageList()) {
//            addEntry(m);
//            sendMessagesToSubscribers();
//        }
//        dummyMsgList.setMessageList(new ArrayList<>());
    }

    private void addEntry(Message message) {
        Long topicId = Long.valueOf(message.getTopicId());

        if (topicId != null) {
            reentrantReadWriteLock.writeLock().lock();
            try {
                MessageList messageList = map.getOrDefault(topicId, new MessageList());
                messageList.add(message);
                map.put(topicId, messageList);
            } finally {
                reentrantReadWriteLock.writeLock().unlock();
            }
        }
    }

    @Async
    public void sendMessagesToSubscribers() throws JsonProcessingException {
        reentrantReadWriteLock.writeLock().lock();
        try {
            for (Long topic : map.keySet()) {
                List<Subscriber> subscribers = getSubscribers(topic);
                for (Subscriber subscriber : subscribers) {
                    sendMessage(subscriber, map.get(topic));
                }
            }
            map.clear();
        } finally {
            reentrantReadWriteLock.writeLock().unlock();
        }
    }

    @Async
    private void sendMessage(Subscriber subscriber, MessageList messageList) throws JsonProcessingException {
        if (subscriber.getSubscriberIp() == null)
            return;

        String url = "http://" + subscriber.getSubscriberIp() + ":8081/subscriber/message";
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);

        String json = mapper.writeValueAsString(messageList);
        HttpEntity<String> entity = new HttpEntity<>(json, headers);
        ResponseEntity<Void> result =
                restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);
    }

    //TODO - to write query to fetch all subscribers + their subscribed topics from db
    public List<Subscriber> getSubscribers(Long topicId) {
        return getDummySubscribers();
    }


    public void updateQueue(String ipAddress) {
        String newUrl = "http://" + ipAddress + ":8080";
        queueUrl = newUrl;
        System.out.println("queueUrl" + queueUrl);
    }

    public String addSubscriber(Subscriber subscriber) {
        //TODO - database call to create new subscriber

        subscriber.setSubscriberId(String.valueOf(UUID.randomUUID()));

        subscriberList.add(subscriber);
        subscriberTopicMap.put(subscriber.getSubscriberId(), subscriber.getTopicList());
        return subscriber.getSubscriberId();
    }

    public boolean registerTopic(Topic topic) {
        String subscriberId = topic.getSubscriberId();
        if (!subscriberTopicMap.containsKey(subscriberId))
            return false;
        Set<String> topics = subscriberTopicMap.get(subscriberId);
        topics.add(topic.getTopicId());
        subscriberTopicMap.put(subscriberId, topics);
        return true;
    }

    public List<Subscriber> getAllSubscribers() {
        return subscriberList;
    }

    //TODO - remove all dummy methods below
    private List<Message> getDummyMsgs() {
        List<Message> list = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            Message msg = new Message(i, "dummy msg");
            list.add(msg);
        }
        return list;
    }

    private void populateDummyMsgsFromQ() {

        dummyMsgList.add(new Message(0, "msg1"));
        dummyMsgList.add(new Message(1, "msg2"));

        dummyMsgList.add(new Message(0, "msg3"));
        dummyMsgList.add(new Message(2, "msg4"));

        dummyMsgList.add(new Message(2, "msg5"));
        dummyMsgList.add(new Message(1, "msg6"));

    }

    private List<Subscriber> getDummySubscribers() {
        List<Subscriber> subscribers = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Set<String> set = new HashSet<>();
            set.add(String.valueOf(i));
            Subscriber s = new Subscriber(set, "127.0.0.1", "123");
            subscribers.add(s);
        }
        return subscribers;
    }
}
