package com.lwl.social_media_platform.mq;

import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

import static com.lwl.social_media_platform.utils.RedisConstant.*;

@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(consumerGroup = "${rocketmq.consumer-treads-push.group}",
        topic = "${rocketmq.producer.topic}",
        selectorExpression = "${rocketmq.consumer-treads-push.selectorExpression}")
public class FollowTreadsPushConsumer implements RocketMQListener<Map<String, String>> {
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void onMessage(Map<String, String> treadsMap) {
        String treadsDtoJSON = treadsMap.get("treadsDtoJSON");
        String userId = (String) JSONUtil.parseObj(treadsDtoJSON).get("userId");
        String treadsId = (String) JSONUtil.parseObj(treadsDtoJSON).get("treadsId");
        Long end = stringRedisTemplate.opsForZSet().size(FOLLOW_LIST_KEY + userId);
        if (end != null && end.intValue() != 0) {
            Set<String> range = stringRedisTemplate.opsForZSet().range(FOLLOW_LIST_KEY + userId, 0, end);
            if (range != null) {
                range.forEach(id -> {
                    if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(USER_LOGIN_KEY + id))) {
                        stringRedisTemplate.opsForZSet().add(TREADS_MAIL_KEY + id, treadsId, System.currentTimeMillis());
                    }
                });
            }
        }
    }
}
