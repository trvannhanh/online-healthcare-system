/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.can.services.impl;

import com.can.pojo.AttachmentType;
import com.can.pojo.ChatAttachment;
import com.can.repositories.ChatAttachmentRepository;
import com.can.repositories.UserRepository;
import com.can.services.ChatAttachmentService;
import com.cloudinary.Cloudinary;
import java.io.File;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author Giidavibe
 */
@Service
@Transactional
public class ChatAttachmentServiceImpl implements ChatAttachmentService {

    @Autowired
    private ChatAttachmentRepository chatAttachmentRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private Cloudinary cloudinary;

    @Override
    public ChatAttachment getChatAttachmentById(int id) {
        return chatAttachmentRepo.getChatAttachmentById(id);
    }

    @Override
    public ChatAttachment getChatAttachmentByMessageId(String messageId) {
        return chatAttachmentRepo.getChatAttachmentByMessageId(messageId);
    }

    @Override
    public ChatAttachment addChatAttachment(String messageId, String chatRoomId, String type, MultipartFile file, int uploadedBy) {
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("Kích thước tệp không được vượt quá 5MB.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.matches("application/vnd\\.openxmlformats-officedocument\\.spreadsheetml\\.sheet|application/vnd\\.ms-excel|image/.*")) {
            throw new IllegalArgumentException("Loại tệp không được hỗ trợ. Chỉ chấp nhận Excel hoặc hình ảnh.");
        }

        try {
            Map<String, Object> options = new HashMap<>();
            if ("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".equals(contentType)
                    || "application/vnd.ms-excel".equals(contentType)) {
                options.put("resource_type", "raw"); 
            } else {
                options.put("resource_type", "auto"); 
            }

            File tempFile = File.createTempFile("upload-", "." + FilenameUtils.getExtension(file.getOriginalFilename()));
            file.transferTo(tempFile);

            Map<String, Object> uploadResult = cloudinary.uploader().upload(tempFile, options);
            String url = uploadResult.get("url").toString();
            String fileName = file.getOriginalFilename();

            tempFile.deleteOnExit();

            ChatAttachment attachment = new ChatAttachment();
            attachment.setMessageId(messageId);
            attachment.setChatRoomId(chatRoomId);
            attachment.setType(AttachmentType.valueOf(type.toUpperCase()));
            attachment.setUrl(url);
            attachment.setFileName(fileName);
            attachment.setUploadedBy(userRepo.getUserById(uploadedBy));
            attachment.setUploadedAt(LocalDateTime.now());

            return chatAttachmentRepo.addChatAttachment(attachment);
        } catch (Exception e) {
            throw new RuntimeException("Không thể upload tệp: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteChatAttachment(int id) {
        return chatAttachmentRepo.deleteChatAttachment(id);
    }
}
