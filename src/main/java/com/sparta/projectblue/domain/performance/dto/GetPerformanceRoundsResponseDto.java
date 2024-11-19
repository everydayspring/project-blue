package com.sparta.projectblue.domain.performance.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.sparta.projectblue.domain.common.enums.PerformanceStatus;

import lombok.Getter;

@Getter
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public class GetPerformanceRoundsResponseDto {

    private List<RoundInfo> rounds;

    // 캐싱을 위한 기본 생성자
    public GetPerformanceRoundsResponseDto() {}

    public GetPerformanceRoundsResponseDto(List<RoundInfo> rounds) {

        this.rounds = rounds;
    }

    @Getter
    public static class RoundInfo {

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime date;

        private PerformanceStatus status;

        public RoundInfo() {}

        public RoundInfo(LocalDateTime date, PerformanceStatus status) {

            this.date = date;
            this.status = status;
        }
    }
}
