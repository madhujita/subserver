package com.consumer.subserver.service;

import com.consumer.subserver.entity.Message;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ConsumerService {

    private void getMessages() {

        // TODO - Read queue until not empty (API from Kothari)
        List<Message> messages = getDummyMsgs();

        // TODO - Get list of topics and subscribers, filtered if possible (Query - Chitalia)

        // Broadcast to subscribers
    }

    private List<Message> getDummyMsgs() {
        List<Message> list = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            Message msg = new Message(i, "dummy msg");
            list.add(msg);
        }
        return list;
    }
}
