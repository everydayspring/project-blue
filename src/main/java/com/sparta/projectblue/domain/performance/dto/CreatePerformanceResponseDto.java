package com.sparta.projectblue.domain.performance.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sparta.projectblue.domain.common.enums.Category;

import lombok.Getter;

@Getter
public class CreatePerformanceResponseDto {

    private final Long performanceId;
    private final String title;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime startDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime endDate;

    private final Long price;
    private final Category category;
    private final String description;
    private final int duration;

    public CreatePerformanceResponseDto(
            Long performanceId,
            String title,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Long price,
            Category category,
            String description,
            int duration) {
        this.performanceId = performanceId;
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.price = price;
        this.category = category;
        this.description = description;
        this.duration = duration;
    }
}
