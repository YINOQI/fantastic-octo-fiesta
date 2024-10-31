package com.lwl.social_media_platform.im;

import cn.hutool.json.JSONUtil;
import com.lwl.social_media_platform.domain.pojo.ChatMessage;
import com.lwl.social_media_platform.service.ChatMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final StringRedisTemplate stringRedisTemplate;
    private final ChatMessageService chatMessageService;

    // 在线用户列表，使用ConcurrentHashMap存储WebSocket连接
    private static final ConcurrentHashMap<String, WebSocketSession> onlineUsers = new ConcurrentHashMap<>();

    public ChatWebSocketHandler(StringRedisTemplate stringRedisTemplate, ChatMessageService chatMessageService) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.chatMessageService = chatMessageService;
    }


    // 当连接建立时触发
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String id = (String)session.getAttributes().get("id");
        onlineUsers.put(id, session);
        List<ChatMessage> notReadList = chatMessageService.getNotRead(Long.parseLong(id));
        if (!notReadList.isEmpty()){
            session.sendMessage(new TextMessage(JSONUtil.toJsonStr(notReadList)));
            notReadList.forEach(message -> message.setRead(true));
            chatMessageService.saveBatch(notReadList);
        }
        log.info("用户连接websocket成功，当前加入用户id为{}",id);
    }

    // 当收到消息时触发
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 处理收到的消息
        String payload = message.getPayload();
        ChatMessage chatMessage = JSONUtil.toBean(payload, ChatMessage.class);

        sendMessageToUser(chatMessage.getReceiverId(),chatMessage);
        chatMessageService.save(chatMessage);
        log.info("收到消息: {}",chatMessage);

    }

    // 当连接关闭时触发
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String id = (String)session.getAttributes().get("id");
        onlineUsers.remove(id);
        log.info("用户退出websocket成功，退出用户id为{}",id);
    }

    // 发送消息给指定用户
    public void sendMessageToUser(Long userId, ChatMessage message) throws Exception {
        WebSocketSession session = onlineUsers.get(userId.toString());
        if (session != null && session.isOpen()) {
            message.setRead(true);
            session.sendMessage(new TextMessage(JSONUtil.toJsonStr(message)));
        }
    }
}
