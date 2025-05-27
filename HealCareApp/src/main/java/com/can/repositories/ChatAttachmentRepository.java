/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.can.repositories;

import com.can.pojo.ChatAttachment;

/**
 *
 * @author Giidavibe
 */
public interface ChatAttachmentRepository {
    ChatAttachment getChatAttachmentById(int id);
    ChatAttachment getChatAttachmentByMessageId(String messageId);
    ChatAttachment addChatAttachment(ChatAttachment attachment);
    boolean deleteChatAttachment(int id);
}
