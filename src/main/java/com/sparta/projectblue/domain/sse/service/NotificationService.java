package com.sparta.projectblue.domain.sse.service;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.sparta.projectblue.aop.SlackNotifier;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final SlackNotifier slackNotifier;

    // 구독 관리 (SseEmitter 연결)
    public SseEmitter subscribe(String userId) {
        SseEmitter emitter = new SseEmitter(60 * 1000L); // 1분 타임아웃(연결유지시간)
        emitters.put(userId, emitter); // userId와 emitter -> Map 에 저장하여 구독 관리

        // 처리 성공 및 타임아웃시 emitter 에서 유저Id 제거
        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));

        return emitter;
    }

    // 특정 사용자에게 알림을 전송하기 위한 메서드
    public void notify(String userId, String title, String message) {
        // SSE 알림 전송
        SseEmitter emitter = emitters.get(userId);
        if (emitter != null) {
            try {
                emitter.send(
                        SseEmitter.event()
                                .name("notification") // 이벤트 이름
                                .data(title + "\n" + message)); // 알림 메시지
            } catch (IOException e) {
                emitters.remove(userId);
            }
        }
        //  Slack 알림 전송
        slackNotifier.sendMessage(title, message);
    }
}
