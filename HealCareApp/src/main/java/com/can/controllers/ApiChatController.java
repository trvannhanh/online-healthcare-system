/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.can.controllers;

import com.can.pojo.ChatAttachment;
import com.can.pojo.User;
import com.can.services.ChatAttachmentService;
import com.can.services.UserService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author Giidavibe
 */

@RestController
@RequestMapping("/api/secure/chat")
@CrossOrigin
public class ApiChatController {
    @Autowired
    private ChatAttachmentService chatAttachmentService;

    @Autowired
    private UserService userService;

    private final FirebaseDatabase firebaseDatabase; 

    @Autowired
    public ApiChatController(FirebaseDatabase firebaseDatabase) {
        this.firebaseDatabase = firebaseDatabase;
    }
    
    
    
    private static final String CHAT_ROOMS_PATH = "chat_rooms";

    @GetMapping("/messages/{otherUserId}")
    public ResponseEntity<List<Map<String, Object>>> getMessages(@PathVariable String otherUserId, Principal principal) {
        try {
            User currentUser = userService.getUserByUsername(principal.getName());
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Collections.singletonList(Map.of("error", "Người dùng không hợp lệ")));
            }
            
            String currentUserId = currentUser.getId().toString();
            String chatRoomId = getChatRoomId(currentUserId, otherUserId);

            DatabaseReference chatRef = firebaseDatabase.getReference(CHAT_ROOMS_PATH + "/" + chatRoomId + "/messages");
            List<Map<String, Object>> messages = new ArrayList<>();

            chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                        Map<String, Object> message = (Map<String, Object>) messageSnapshot.getValue();
                        message.put("id", messageSnapshot.getKey());
                        messages.add(message);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    throw new RuntimeException("Lỗi khi lấy tin nhắn: " + databaseError.getMessage());
                }
            });

            Thread.sleep(1000); 
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonList(Map.of("error", "Lỗi khi lấy tin nhắn: " + e.getMessage())));
        }
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(
            @RequestParam(value = "text", required = false) String text,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "otherUserId") String otherUserId,
            Principal principal) {

        try {
            User currentUser = userService.getUserByUsername(principal.getName());
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Người dùng không hợp lệ");
            }
            String currentUserId = currentUser.getId().toString();
            String chatRoomId = getChatRoomId(currentUserId, otherUserId);

            DatabaseReference chatRef = firebaseDatabase.getReference(CHAT_ROOMS_PATH + "/" + chatRoomId + "/messages");
            String messageId = chatRef.push().getKey();

            Map<String, Object> messageData = new HashMap<>();
            messageData.put("senderId", currentUserId);
            messageData.put("senderName", currentUser.getFirstName() + " " + (currentUser.getLastName() != null ? currentUser.getLastName() : ""));
            messageData.put("timestamp", System.currentTimeMillis());

            if (text != null && !text.trim().isEmpty()) {
                messageData.put("type", "text");
                messageData.put("text", text.trim());
            } else if (file != null && !file.isEmpty()) {
                ChatAttachment attachment = chatAttachmentService.addChatAttachment(
                    messageId, chatRoomId, "file", file, currentUser.getId()
                );
                messageData.put("type", "file");
                messageData.put("attachmentId", attachment.getId());
                messageData.put("fileUrl", attachment.getUrl());
                messageData.put("fileName", attachment.getFileName());
            } else if (image != null && !image.isEmpty()) {
                ChatAttachment attachment = chatAttachmentService.addChatAttachment(
                    messageId, chatRoomId, "image", image, currentUser.getId()
                );
                messageData.put("type", "image");
                messageData.put("attachmentId", attachment.getId());
                messageData.put("fileUrl", attachment.getUrl());
                messageData.put("fileName", attachment.getFileName());
            } else {
                return ResponseEntity.badRequest().body("Không có nội dung tin nhắn.");
            }

            chatRef.child(messageId).setValueAsync(messageData);

            DatabaseReference participantsRef = firebaseDatabase.getReference(CHAT_ROOMS_PATH + "/" + chatRoomId + "/participants");
            Map<String, String> participants = new HashMap<>();
            participants.put("userId", currentUserId);
            participants.put("otherUserId", otherUserId);
            participantsRef.setValueAsync(participants);

            return ResponseEntity.ok(Map.of("message", "Tin nhắn đã được gửi", "messageId", messageId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi gửi tin nhắn: " + e.getMessage());
        }
    }

    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<?> deleteMessage(@PathVariable String messageId, @RequestParam String otherUserId, Principal principal) {
        try {
            User currentUser = userService.getUserByUsername(principal.getName());
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Người dùng không hợp lệ");
            }
            String currentUserId = currentUser.getId().toString();
            String chatRoomId = getChatRoomId(currentUserId, otherUserId);

            DatabaseReference messageRef = firebaseDatabase.getReference(CHAT_ROOMS_PATH + "/" + chatRoomId + "/messages/" + messageId);
            messageRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Map<String, Object> message = (Map<String, Object>) dataSnapshot.getValue();
                        if (message.get("senderId").equals(currentUserId)) {
                            messageRef.removeValueAsync();
                            if (message.containsKey("attachmentId")) {
                                ChatAttachment attachment = chatAttachmentService.getChatAttachmentById(
                                    Integer.parseInt(message.get("attachmentId").toString())
                                );
                                if (attachment != null) {
                                    String publicId = attachment.getUrl().substring(attachment.getUrl().lastIndexOf("/") + 1, attachment.getUrl().lastIndexOf("."));
                                    chatAttachmentService.deleteChatAttachment(attachment.getId()); 
                                }
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    throw new RuntimeException("Lỗi khi xóa tin nhắn: " + databaseError.getMessage());
                }
            });

            return ResponseEntity.ok(Map.of("message", "Tin nhắn đã được xóa"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi xóa tin nhắn: " + e.getMessage());
        }
    }

    private String getChatRoomId(String userId, String otherUserId) {
        return userId.compareTo(otherUserId) < 0 ? userId + "_" + otherUserId : otherUserId + "_" + userId;
    }
}
