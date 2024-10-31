package com.lwl.social_media_platform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lwl.social_media_platform.domain.pojo.ChatMessage;
import com.lwl.social_media_platform.mapper.ChatMessageMapper;
import com.lwl.social_media_platform.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements ChatMessageService {
    @Override
    public List<ChatMessage> getNotRead(Long userId) {
        return this.lambdaQuery().eq(ChatMessage::getReceiverId, userId).eq(ChatMessage::isRead, false).list();
    }
}
