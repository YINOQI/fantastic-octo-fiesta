package com.lwl.social_media_platform.scheduler;

import cn.hutool.json.JSONUtil;
import com.lwl.social_media_platform.domain.pojo.Support;
import com.lwl.social_media_platform.domain.pojo.Treads;
import com.lwl.social_media_platform.service.SupportService;
import com.lwl.social_media_platform.service.TreadsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.lwl.social_media_platform.utils.RedisConstant.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class SupportScheduler {
    private final StringRedisTemplate stringRedisTemplate;
    private final SupportService supportService;
    private final TreadsService treadsService;
    private long start;
    private long end;

    //    @Async("supportTaskExecutor")
    @Scheduled(cron = "0 0 0/1 * * ?")
    public void updateSupport() {
        // 定时任务具体业务逻辑
        Set<String> keys = stringRedisTemplate.keys(SUPPORT_SCHEDULER_KEY + "*");
        Long length = stringRedisTemplate.opsForZSet().zCard(SUPPORT_SCHEDULER_TREAD_KEY);
        if (length == null) {
            return;
        }
        for (int i = 0; i < length.intValue() / 100; i++) {
            start = i;
            end = i + i * 100;
            Set<String> stringSet = stringRedisTemplate.opsForZSet().range(SUPPORT_SCHEDULER_KEY, start, end);
            update(stringSet);
        }

//        if (keys == null) {
//            return;
//        }
//        keys.forEach(key -> {
//            Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(key);
//            if (entries.isEmpty()) {
//                return;
//            }
//            List<Support> cancelList = new ArrayList<>();
//            List<Support> supportsList = new ArrayList<>();
//            entries.forEach((supportKey, supportObj) -> {
//                String supportStr = (String) supportObj;
//                Support support = JSONUtil.toBean(supportStr, Support.class);
//                if (support.getIsCancel() == 1) {
//                    supportsList.add(support);
//                } else {
//                    cancelList.add(support);
//                }
//            });
//            supportService.saveBatch(supportsList);
//            supportService.removeBatchByIds(cancelList);
//            String treadsId = key.split(SUPPORT_SCHEDULER_KEY)[1];
//            treadsService.lambdaUpdate()
//                    .eq(Treads::getId, treadsId)
//                    .setIncrBy(Treads::getSupportNum, supportsList.size() - cancelList.size())
//                    .update();
//        });
//        stringRedisTemplate.delete(keys);
    }

    private void update(Set<String> supportSet) {
        if (supportSet == null) {
            return;
        }
        supportSet.forEach(key -> {
            Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(SUPPORT_KEY + key);
            if (entries.isEmpty()) {
                return;
            }
            List<Support> cancelList = new ArrayList<>();
            List<Support> supportsList = new ArrayList<>();
            entries.forEach((supportKey, supportObj) -> {
                String supportStr = (String) supportObj;
                Support support = JSONUtil.toBean(supportStr, Support.class);
                if (support.getIsCancel() == 1) {
                    supportsList.add(support);
                } else {
                    cancelList.add(support);
                }
            });
            supportService.saveBatch(supportsList);
            supportService.removeBatchByIds(cancelList);
            String treadsId = key.split(SUPPORT_SCHEDULER_KEY)[1];
            treadsService.lambdaUpdate()
                    .eq(Treads::getId, treadsId)
                    .setIncrBy(Treads::getSupportNum, supportsList.size() - cancelList.size())
                    .update();
        });
        stringRedisTemplate.delete(supportSet);
    }
}
