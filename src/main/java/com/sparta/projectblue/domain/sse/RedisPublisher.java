package com.sparta.projectblue.domain.sse;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RedisPublisher {

    private final StringRedisTemplate redisTemplate;

    // 알림 메세지를 Redis 로 전송
    public void publishNotification(String channel, String message) {
        redisTemplate.convertAndSend(channel, message);
    }
}
