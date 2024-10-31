package com.lwl.social_media_platform.domain.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatMessage {
    private Long id;       // 消息的唯一标识符
    private Long senderId;        // 发送者的用户ID
    private Long receiverId;      // 接收者的用户ID
    private String content;         // 消息内容
    private MessageType messageType; // 消息类型（文本、图片、文件等）
    private LocalDateTime timestamp; // 发送时间
    private boolean isRead;         // 是否已读


    public enum MessageType {
        TEXT, IMAGE
    }
}
