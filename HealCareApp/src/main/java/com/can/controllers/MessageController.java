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
@RequestMapping("/api")
public class MessageController {

    @Autowired
    private MessageService messageService;

    // Lấy tin nhắn theo id
    @GetMapping("/messages/{messageId}")
    public ResponseEntity<Messages> getMessageById(@PathVariable Integer messageId) {
        Messages message = messageService.getMessageById(messageId);
        return message != null ? ResponseEntity.ok(message) : ResponseEntity.notFound().build();
    }

    // Lấy danh sách tin nhắn theo người gửi
    @GetMapping("/messages/sender/{senderId}")
    public ResponseEntity<List<Messages>> getMessagesBySender(@PathVariable Integer senderId) {
        try {
            User sender = new User(); // Bạn cần lấy thông tin người gửi từ database.
            sender.setId(senderId);
            List<Messages> messages = messageService.getMessagesBySender(sender);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Lấy danh sách tin nhắn theo người nhận
    @GetMapping("/messages/receiver/{receiverId}")
    public ResponseEntity<List<Messages>> getMessagesByReceiver(@PathVariable Integer receiverId) {
        try {
            User receiver = new User(); // Bạn cần lấy thông tin người nhận từ database.
            receiver.setId(receiverId);
            List<Messages> messages = messageService.getMessagesByReceiver(receiver);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Lấy tin nhắn theo tham số động (chỉ cần dùng Map)
    @GetMapping("/messages")
    public ResponseEntity<List<Messages>> getMessagesByCriteria(@RequestParam Map<String, String> params) {
        try {
            List<Messages> messages = messageService.getMessages(params);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Lấy tin nhắn theo timestamp (ngày tháng cụ thể)
    @GetMapping("/messages/timestamp")
    public ResponseEntity<List<Messages>> getMessagesByTimestamp(@RequestParam Date timestamp) {
        try {
            List<Messages> messages = messageService.getMessagesByTimestamp(timestamp);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

}
