/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.can.services;

import com.can.pojo.ChatAttachment;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author Giidavibe
 */
public interface ChatAttachmentService {
    ChatAttachment getChatAttachmentById(int id);
    ChatAttachment getChatAttachmentByMessageId(String messageId);
    ChatAttachment addChatAttachment(String messageId, String chatRoomId, String type, MultipartFile file, int uploadedBy);
    boolean deleteChatAttachment(int id);
}
