package com.maillSaviour.demo.Service;

import com.maillSaviour.demo.Entity.MessageEntity;
import com.maillSaviour.demo.Repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    public String getMessageContentByName(String messageName) {
        // Find the message by messageName
        MessageEntity message = messageRepository.findByMessageName(messageName)
                .orElseThrow(() -> new IllegalArgumentException("Message with name '" + messageName + "' not found."));

        // Return the message content
        return message.getMeassageContent();
    }
}