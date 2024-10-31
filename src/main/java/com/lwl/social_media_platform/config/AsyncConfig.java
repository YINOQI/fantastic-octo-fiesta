package com.lwl.social_media_platform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

@EnableAsync
@Configuration
public class AsyncConfig {
    @Bean("supportTaskExecutor")
    public ThreadPoolTaskExecutor threadPoolTaskExecutor(){
        ThreadPoolTaskExecutor threadPoolTaskExecutor =new ThreadPoolTaskExecutor();
        // 核心线程数
        threadPoolTaskExecutor.setCorePoolSize(20);
        // 最大线程数
        threadPoolTaskExecutor.setMaxPoolSize(50);
        // 队列最大长度
        threadPoolTaskExecutor.setQueueCapacity(100);
        // 线程池维护线程所允许的空闲时间
        threadPoolTaskExecutor.setKeepAliveSeconds(100);
        // 线程前缀
        threadPoolTaskExecutor.setThreadNamePrefix("supportThread-");
        // 线程池对拒绝任务(无线程可用)的处理策略
        threadPoolTaskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        threadPoolTaskExecutor.initialize();
        return threadPoolTaskExecutor;
    }
}
