package com.sparta.projectblue.domain.performance.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.sparta.projectblue.domain.common.enums.Category;
import com.sparta.projectblue.domain.hall.entity.Hall;
import com.sparta.projectblue.domain.performance.entity.Performance;
import com.sparta.projectblue.domain.poster.entity.Poster;

import lombok.Getter;

@Getter
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public class GetPerformanceResponseDto {

    private String title;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endDate;

    private Long price;
    private Category category;
    private String description;
    private int duration;
    private String hallName;
    private String posterName;
    private String imageUrl;

    public GetPerformanceResponseDto(Performance performance, Hall hall, Poster poster) {

        this.title = performance.getTitle();
        this.startDate = performance.getStartDate();
        this.endDate = performance.getEndDate();
        this.price = performance.getPrice();
        this.category = performance.getCategory();
        this.description = performance.getDescription();
        this.duration = performance.getDuration();
        this.hallName = hall.getName();
        this.posterName = poster.getName();
        this.imageUrl = poster.getImageUrl();
    }

    public GetPerformanceResponseDto() {}
}
