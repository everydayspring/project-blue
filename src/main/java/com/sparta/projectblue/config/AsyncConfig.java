package com.sparta.projectblue.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    @Bean(name = "mailExecutor")
    public Executor mailExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(50); // 기본 쓰레드
        executor.setMaxPoolSize(200); // 동시에 동작하는 최대 쓰레드
        executor.setQueueCapacity(1000); // CorePool 크기를 넘어설때 큐에 저장되는데, 큐의 최대 용량
        executor.setThreadNamePrefix("Async MailExecutor - ");
        executor.initialize();

        return executor;
    }
}
