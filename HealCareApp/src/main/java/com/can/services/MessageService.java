package com.can.services;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.can.pojo.Messages;
import com.can.pojo.User;

/**
 *
 * @author Giidavibe
 */
public interface MessageService {
    Messages getMessageById(Integer id);
    List<Messages> getMessagesBySender(User sender);
    List<Messages> getMessagesByReceiver(User receiver);
    List<Messages> getMessages(Map<String, String> params);
    List<Messages> getMessagesByTimestamp(Date timestamp);
}
