package com.sparta.projectblue.sse.controller;

import com.sparta.projectblue.sse.RedisPublisher;
import com.sparta.projectblue.sse.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final RedisPublisher redisPublisher;

    // 클라이언트 쪽에서 알림을 구독하기 위한 엔드포인트
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> subscribe(
            @RequestParam String userId) {
        // 사용자가 SSE 구독 요청을 보내면, 구독한 사용자에 대한 Emitter 반환
        return ResponseEntity.ok(notificationService.subscribe(userId));
    }

    @PostMapping("/send")
    public ResponseEntity<String> sendNotifications(
            @RequestParam String userId,
            @RequestParam String title,
            @RequestParam String message) {
        String formattedMessage = userId + "::" + title + "::" + message;
        redisPublisher.publishNotification("notification-channel", formattedMessage);

        return ResponseEntity.ok("유저 알림 전송" + userId);
    }
}