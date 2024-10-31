package com.lwl.social_media_platform.im;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ServerEndpoint(value = "/chat-server/{userId}")
public class WebSocketServer {
//    private final StringRedisTemplate stringRedisTemplate;
    public static final Map<String, Session> sessionMap = new ConcurrentHashMap<>();

    public WebSocketServer(){}

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        System.out.println("成功连接");
        sessionMap.put(userId, session);
    }

    @OnClose
    public void onClose(Session session, @PathParam("userId") String userId){
        sessionMap.remove(userId);
    }

    @OnMessage
    public void onMessage(String message,Session session, @PathParam("userId") String userId){
        JSONObject messageObj = JSONUtil.parseObj(message);
        String toUser = messageObj.getStr("toUser");
        String text = messageObj.getStr("text");
        if (sessionMap.containsKey(toUser)) {
            Session toSession = sessionMap.get(toUser);
        }else {
        }

    }
}
