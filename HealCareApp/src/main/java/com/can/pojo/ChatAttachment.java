/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.can.pojo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.time.LocalDateTime;

/**
 *
 * @author Giidavibe
 */

@Entity
@Table(name = "chat_attachments")
public class ChatAttachment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "message_id" ,nullable = false)
    private String messageId;

    @Column(name = "chat_room_id",nullable = false)
    private String chatRoomId;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private AttachmentType type;

    @Column(name = "url", nullable = false)
    private String url;
    
    @Column(name = "file_name")
    private String fileName;
    
    @ManyToOne
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploadedBy;

    @Column(name = "uploaded_at",nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime uploadedAt;

    /**
     * @return the id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * @return the messageId
     */
    public String getMessageId() {
        return messageId;
    }

    /**
     * @param messageId the messageId to set
     */
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    /**
     * @return the chatRoomId
     */
    public String getChatRoomId() {
        return chatRoomId;
    }

    /**
     * @param chatRoomId the chatRoomId to set
     */
    public void setChatRoomId(String chatRoomId) {
        this.chatRoomId = chatRoomId;
    }

    /**
     * @return the type
     */
    public AttachmentType getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(AttachmentType type) {
        this.type = type;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return the fileName
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @param fileName the fileName to set
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * @return the uploadedBy
     */
    public User getUploadedBy() {
        return uploadedBy;
    }

    /**
     * @param uploadedBy the uploadedBy to set
     */
    public void setUploadedBy(User uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    /**
     * @return the uploadedAt
     */
    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    /**
     * @param uploadedAt the uploadedAt to set
     */
    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}
