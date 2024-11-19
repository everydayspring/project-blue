package com.sparta.projectblue.domain.performance.dto;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreatePerformanceRequestDto {

    @Schema(example = "Jekyll ＆ Hyde")
    @NotNull
    private String title;

    @Schema(example = "2024-11-29")
    @NotNull
    private String startDate;

    @Schema(example = "2025-05-18")
    @NotNull
    private String endDate;

    @Schema(example = "170000")
    @NotNull
    private Long price;

    @Schema(example = "CONCERT, MUSICAL, SPORTS")
    @Enumerated(EnumType.STRING)
    @NotNull
    private String category;

    @Schema(example = "대한민국 뮤지컬 역사를 이끌어온 독보적 월드 클래스 뮤지컬 <지킬앤하이드> 20주년 공연")
    @NotNull
    private String description;

    @Schema(example = "1")
    @NotNull
    private Long hallId;

    @Schema(example = "170")
    @NotNull
    private int duration;

    @Schema(example = "[1, 2, 3]")
    private Long[] performers;
}
