package com.sparta.projectblue.aop;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SlackNotifier {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${slack.webhook.url}")
    private String slackWebhookUrl;

    public void sendMessage(String title, String message) {

        Map<String, Object> payload = new HashMap<>();

        String formattedMessage = "*" + title + "*\n" + message;

        payload.put("text", formattedMessage);
        payload.put("username", "티켓");

        try {
            restTemplate.postForObject(slackWebhookUrl, payload, String.class);
        } catch (Exception e) {
            log.error("Slack 알림 전송 오류: {}", e.getMessage(), e);
        }
    }
}
