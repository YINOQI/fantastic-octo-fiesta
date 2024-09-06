package com.lwl.social_media_platform.scheduler;

import com.lwl.social_media_platform.service.SupportService;
import com.lwl.social_media_platform.service.TreadsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

import static com.lwl.social_media_platform.utils.RedisConstant.SUPPORT_KEY;

@Slf4j
@Component
@RequiredArgsConstructor
public class SupportScheduler {
    private final StringRedisTemplate stringRedisTemplate;
    private final SupportService supportService;
    private final TreadsService treadsService;
    @Async("supportTaskExecutor")
    @Scheduled(cron = "0 0 0/1 * * ?")
    public void updateSupport(){
        // 定时任务具体业务逻辑
        Set<String> keys = stringRedisTemplate.keys(SUPPORT_KEY + "*");
        if (keys == null) {
            return;
        }
        keys.forEach(key -> {
            Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(key);
            entries.forEach((id, value) -> {

            });
        });
    }
}
