//package com.lwl.social_media_platform.scheduler;
//
//import com.lwl.social_media_platform.service.SupportService;
//import com.lwl.social_media_platform.service.TreadsService;
//import lombok.Getter;
//import lombok.RequiredArgsConstructor;
//import lombok.Setter;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.data.redis.core.StringRedisTemplate;
//import org.springframework.scheduling.annotation.SchedulingConfigurer;
//import org.springframework.scheduling.config.ScheduledTaskRegistrar;
//import org.springframework.scheduling.support.CronTrigger;
//import org.springframework.stereotype.Component;
//
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.Map;
//import java.util.Set;
//import java.util.concurrent.*;
//
//import static com.lwl.social_media_platform.utils.RedisConstant.SUPPORT_KEY;
//
//
//@Component
//@Slf4j
//@Setter
//@Getter
//@RequiredArgsConstructor
//public class SupportUpdateScheduler implements SchedulingConfigurer {
//    public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
//    private final StringRedisTemplate stringRedisTemplate;
//    private final TreadsService treadsService;
//    private final SupportService supportService;
//
//    @Value("${scheduler.support-update}")
//    private String cron;
//    private final ExecutorService executorService = new ThreadPoolExecutor(
//            Runtime.getRuntime().availableProcessors(),
//            Runtime.getRuntime().availableProcessors() << 1,
//            60,
//            TimeUnit.SECONDS,
//            new SynchronousQueue<>(),
//            new ThreadPoolExecutor.DiscardPolicy()
//    );
//
//
//    @Override
//    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
//        taskRegistrar.setScheduler(Executors.newScheduledThreadPool(10));
//        taskRegistrar.addTriggerTask(() -> {
//            log.info(">>>>>>定时任务开始时间： {}", LocalDateTime.now().format(DateTimeFormatter.ofPattern(YYYY_MM_DD_HH_MM_SS)));
//            try {
//                // 定时任务具体业务逻辑
//                Set<String> keys = stringRedisTemplate.keys(SUPPORT_KEY + "*");
//                if (keys == null) {
//                    return;
//                }
//                keys.forEach(key ->{
//                    Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(key);
//                    entries.forEach((id,value) ->{
//
//                    });
//                });
//
//                log.info("定时任务具体业务逻辑，模拟业务逻辑处理......");
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                log.error("发送邮件定时任务处理失败", e);
//                Thread.currentThread().interrupt();
//            }
//            log.info(">>>>>>定时任务结束时间： {}", LocalDateTime.now().format(DateTimeFormatter.ofPattern(YYYY_MM_DD_HH_MM_SS)));
//        }, triggerContext -> {
//            // 使用CronTrigger触发器，可动态修改cron表达式来操作循环规则
//            CronTrigger cronTrigger = new CronTrigger(cron);
//            return cronTrigger.nextExecution(triggerContext);
//        });
//    }
//}
