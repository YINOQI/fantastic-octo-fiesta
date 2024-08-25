package com.lwl.social_media_platform.mq;

import cn.hutool.json.JSONUtil;
import com.lwl.social_media_platform.domain.dto.TreadsDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class TreadsProducer {

    private final RocketMQTemplate rocketMQTemplate;

    @Value("${rocketmq.producer.topic}")
    private String topic;

    public void sendTreadMessage(String treadsDtoJSON) {
        String keys = UUID.randomUUID().toString();
        Message<Map<String, String>> build = buildMessage(keys, treadsDtoJSON);
        SendResult sendResult;
        try {
            sendResult = rocketMQTemplate.syncSend(topic + ":" + "insert", build, 2000L);
            log.info("动态发送结果：{}，消息ID：{}，消息Keys：{}", sendResult.getSendStatus(), sendResult.getMsgId(), keys);
        } catch (Throwable ex) {
            log.error("[消息访问统计监控] 消息发送失败", ex);
        }
    }

    public void sendTreadUpdateMessage(TreadsDTO treadsDTO) {
        String keys = UUID.randomUUID().toString();
        Message<Map<String, String>> build = buildMessage(keys, JSONUtil.toJsonStr(treadsDTO));
        SendResult sendResult;
        try {
            sendResult = rocketMQTemplate.syncSend(topic + ":" + "update", build, 2000L);
            log.info("动态发送结果：{}，消息ID：{}，消息Keys：{}", sendResult.getSendStatus(), sendResult.getMsgId(), keys);
        } catch (Throwable ex) {
            log.error("[消息访问统计监控] 消息发送失败", ex);
        }
    }

    private Message<Map<String, String>> buildMessage(String keys, String treadsDtoJSON) {
        Map<String, String> keyMap = new HashMap<>();
        keyMap.put("keys", keys);
        keyMap.put("treadsDtoJSON", treadsDtoJSON);
        return MessageBuilder
                .withPayload(keyMap)
                .setHeader(MessageConst.PROPERTY_KEYS, keys)
                .build();
    }

}
