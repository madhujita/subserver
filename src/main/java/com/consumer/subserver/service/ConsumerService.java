package com.consumer.subserver.service;

import com.consumer.subserver.entity.Message;
import com.consumer.subserver.entity.MessageList;
import com.consumer.subserver.entity.Subscriber;
import com.consumer.subserver.entity.Topic;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
public class ConsumerService {

    static private HashMap<String, MessageList> map;
    static private List<Subscriber> subscriberList;
    static private Map<String, Set<String>> subscriberTopicMap;
    private ReentrantReadWriteLock reentrantReadWriteLock;
    ObjectMapper mapper;
    private String queueIpAddress;

    public ConsumerService() {
        queueIpAddress = "localhost:9000";
        map = new HashMap<>();
        reentrantReadWriteLock = new ReentrantReadWriteLock();
        mapper = new ObjectMapper();
        subscriberList = new ArrayList<>();
        subscriberTopicMap = new HashMap<>();
    }

    @Async
    public void fetchMessage() {
        RestTemplate restTemplate = new RestTemplate();

        List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();

        List<MediaType> mediaTypeList = new ArrayList<>();
        mediaTypeList.add(MediaType.valueOf("text/html;charset=UTF-8"));
        mediaTypeList.add(MediaType.APPLICATION_JSON);
        converter.setSupportedMediaTypes(mediaTypeList);
        messageConverters.add(converter);
        restTemplate.setMessageConverters(messageConverters);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.ALL));

        HttpEntity<String> entity = new HttpEntity<>("parameters", headers);
        while (true) {
            String queueUrl = "http://" + queueIpAddress + "/nextMessage";
            try {
                ResponseEntity<Message> result =
                        restTemplate.exchange(queueUrl, HttpMethod.GET, entity, Message.class);
                Message message = result.getBody();
                addEntry(message);
                sendMessagesToSubscribers();
            } catch (Exception e) {
                return;
            }
        }
    }

    private void addEntry(Message message) {
        String topic = message.getTopic();

        if (topic != null) {
            reentrantReadWriteLock.writeLock().lock();
            try {
                MessageList messageList = map.getOrDefault(topic, new MessageList());
                messageList.add(message);
                map.put(topic, messageList);
            } finally {
                reentrantReadWriteLock.writeLock().unlock();
            }
        }
    }

    @Async
    public void sendMessagesToSubscribers() throws JsonProcessingException {
        reentrantReadWriteLock.writeLock().lock();
        try {
            for (String topic : map.keySet()) {
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
        RestTemplate restTemplate = new RestTemplate();

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
    public List<Subscriber> getSubscribers(String topicId) {
        return getDummySubscribers();
    }


    public void updateQueue(String ipAddress) {
        queueIpAddress = ipAddress;
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
