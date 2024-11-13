package com.sparta.projectblue.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    @Bean(name = "mailExecutor")
    public Executor mailExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5); // 기본 쓰레드
        executor.setMaxPoolSize(10); // 동시에 동작하는 최대 쓰레드
        executor.setQueueCapacity(100); // CorePool 크기를 넘어설때 큐에 저장되는데, 큐의 최대 용량
        executor.setThreadNamePrefix("Async MAilExecutor - ");
        executor.initialize();

        logThreadPoolStatus(executor);

        return executor;
    }

    private void logThreadPoolStatus(ThreadPoolTaskExecutor executor) {
        System.out.println("Active Threads : " + executor.getActiveCount());
        System.out.println("Total Tasks : " + executor.getThreadPoolExecutor().getTaskCount());
        System.out.println("Completed Tasks : " + executor.getThreadPoolExecutor().getCompletedTaskCount());
    }
}
