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
import java.util.concurrent.*;

import static com.lwl.social_media_platform.utils.RedisConstant.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class SupportScheduler {
    private final StringRedisTemplate stringRedisTemplate;
    private final SupportService supportService;
    private final TreadsService treadsService;
    private final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
            100, 400, 100,
            TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(100),
            new ThreadPoolExecutor.DiscardPolicy()
    );

    //    @Async("supportTaskExecutor")
    @Scheduled(cron = "0 0/5 * * * ? ")
    public void updateSupport() {
        log.info("开始定时任务，当前时间为{}", new Date());
        // 定时任务具体业务逻辑

        Long length = stringRedisTemplate.opsForZSet().zCard(SUPPORT_SCHEDULER_TREAD_KEY);
        if (length == null) {
            return;
        }

        Set<String> hotTreadIdKeys = stringRedisTemplate.opsForZSet().reverseRangeByScore(SUPPORT_SCHEDULER_TREAD_KEY, 100000, Double.POSITIVE_INFINITY);
        if (hotTreadIdKeys != null) {
            log.info("开始高赞动态数据持久化任务，当前时间为{}", new Date());
            CountDownLatch countDownLatch = new CountDownLatch(hotTreadIdKeys.size());
            long startTimeMillis = System.currentTimeMillis();
            hotTreadIdKeys.forEach(treadId -> {

                Long size = stringRedisTemplate.opsForZSet().zCard(SUPPORT_SCHEDULER_KEY + treadId);
                if (size != null) {
                    threadPoolExecutor.submit(() -> {
                        update(treadId);
                        countDownLatch.countDown();
                    });
                } else {
                    countDownLatch.countDown();
                }
            });

            try {
                countDownLatch.await();

                long endTimeMillis = System.currentTimeMillis();
                log.info("高赞动态数据持久化任务完成，当前时间为{}", new Date());
                log.info("高赞动态数据持久化任务总用时时间为{}", endTimeMillis - startTimeMillis);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        Long commonSize = stringRedisTemplate.opsForZSet().count(SUPPORT_SCHEDULER_TREAD_KEY, 0, 100000);
        if (commonSize != null) {
            log.info("开始普通动态数据持久化任务，当前时间为{}", new Date());
            long startTimeMillis = System.currentTimeMillis();
            int count = 10;
            CountDownLatch countDownLatch = new CountDownLatch(commonSize.intValue() / count + 1);
            for (int offset = 0; offset < commonSize / count + 1; offset++) {
                Set<String> treadIdKeys = stringRedisTemplate.opsForZSet().reverseRangeByScore(SUPPORT_SCHEDULER_TREAD_KEY, 0, 100000, offset, count);
                if (treadIdKeys != null) {
                    threadPoolExecutor.submit(() -> {
                        update(treadIdKeys);
                        countDownLatch.countDown();
                    });
                } else {
                    countDownLatch.countDown();
                }
            }
            try {
                countDownLatch.await();

                long endTimeMillis = System.currentTimeMillis();
                log.info("普通动态数据持久化任务完成，当前时间为{}", new Date());
                log.info("普通动态数据持久化任务总用时时间为{}", endTimeMillis - startTimeMillis);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void update(Set<String> treadIdKeys) {
        treadIdKeys.forEach(treadId -> {
            Long size = stringRedisTemplate.opsForZSet().zCard(SUPPORT_SCHEDULER_KEY + treadId);
            if (size != null) {
                update(treadId);
            }
        });
    }

    private void update(String treadId) {
        Map<Object, Object> supportMap = stringRedisTemplate.opsForHash().entries(SUPPORT_KEY + treadId);
        if (supportMap.isEmpty()) {
            return;
        }

        List<Support> cancelList = new ArrayList<>();
        List<Support> supportsList = new ArrayList<>();

        supportMap.forEach((userId, supportObj) -> {
            String supportStr = (String) supportObj;
            Support support = JSONUtil.toBean(supportStr, Support.class);
            if (support.getIsCancel() == 1) {
                supportsList.add(support);
            } else {
                cancelList.add(support);
            }
        });

        int support = 0;
        int cancel = 0;
        if (!supportsList.isEmpty()) {
            supportService.saveBatch(supportsList);
            support = supportsList.size();
        }

        if (!cancelList.isEmpty()) {
            supportService.removeBatchByIds(cancelList);
            cancel = cancelList.size();
        }

        treadsService.lambdaUpdate()
                .eq(Treads::getId, treadId)
                .setIncrBy(Treads::getSupportNum, support - cancel)
                .update();

        stringRedisTemplate.delete(SUPPORT_KEY + treadId);
    }
}
