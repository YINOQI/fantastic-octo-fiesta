package com.lwl.social_media_platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lwl.social_media_platform.domain.pojo.ChatMessage;

import java.util.List;

public interface ChatMessageService extends IService<ChatMessage> {

    List<ChatMessage> getNotRead(Long userId);
}
