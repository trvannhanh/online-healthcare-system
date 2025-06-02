package com.can.services.impl;

import com.can.services.MessageService;
import com.can.pojo.Messages;
import com.can.pojo.User;
import com.can.repositories.MessageRepository;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author DELL
 */
@Service
public class MessageServiceImpl implements MessageService{
    @Autowired
    private MessageRepository messageRepository;

    @Override
    public Messages getMessageById(Integer id) {
        return this.messageRepository.getMessageById(id);
    }

    @Override
    public List<Messages> getMessagesBySender(User sender) {
        return this.messageRepository.getMessagesBySender(sender);
    }

    @Override
    public List<Messages> getMessagesByReceiver(User receiver) {
        return this.messageRepository.getMessagesByReceiver(receiver);
    }

    @Override
    public List<Messages> getMessages(Map<String, String> params) {
        return this.messageRepository.getMessages(params);
    }

    @Override
    public List<Messages> getMessagesByTimestamp(Date timestamp) {
        return this.messageRepository.getMessagesByTimestamp(timestamp);
    }
}
