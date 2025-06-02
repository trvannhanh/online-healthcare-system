package com.can.controllers;

import com.can.pojo.Messages;
import com.can.pojo.User;
import com.can.services.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *
 * @author DELL
 */
@RestController
@RequestMapping("/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @GetMapping("/{messageId}")
    public ResponseEntity<Messages> getMessageById(@PathVariable Integer messageId) {
        Messages message = messageService.getMessageById(messageId);
        return message != null ? ResponseEntity.ok(message) : ResponseEntity.notFound().build();
    }

    @GetMapping("/sender/{senderId}")
    public ResponseEntity<List<Messages>> getMessagesBySender(@PathVariable Integer senderId) {
        try {
            User sender = new User(); 
            sender.setId(senderId);
            List<Messages> messages = messageService.getMessagesBySender(sender);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/receiver/{receiverId}")
    public ResponseEntity<List<Messages>> getMessagesByReceiver(@PathVariable Integer receiverId) {
        try {
            User receiver = new User(); 
            receiver.setId(receiverId);
            List<Messages> messages = messageService.getMessagesByReceiver(receiver);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<Messages>> getMessagesByCriteria(@RequestParam Map<String, String> params) {
        try {
            List<Messages> messages = messageService.getMessages(params);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/timestamp")
    public ResponseEntity<List<Messages>> getMessagesByTimestamp(@RequestParam Date timestamp) {
        try {
            List<Messages> messages = messageService.getMessagesByTimestamp(timestamp);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

}
