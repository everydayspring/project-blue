package com.sparta.projectblue.domain.performance.dto;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;

import com.sparta.projectblue.domain.common.enums.Category;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePerformanceRequestDto {

    @Schema(example = "TWENTY")
    @NotNull
    private String title;

    @Schema(example = "2024-12-14")
    @NotNull
    private String startDate;

    @Schema(example = "2024-12-15")
    @NotNull
    private String endDate;

    @Schema(example = "132000")
    @NotNull
    private Long price;

    @Schema(example = "CONCERT, MUSICAL, SPORTS")
    @Enumerated(EnumType.STRING)
    @NotNull
    private Category category;

    @Schema(example = "불법 거래로 발생한 피해의 책임은 전적으로 거래 당사자에게 있습니다.")
    @NotNull
    private String description;

    @Schema(example = "1")
    @NotNull
    private Long hallId;

    @Schema(example = "120")
    @NotNull
    private int duration;

    @Schema(example = "[1, 2, 3]")
    private Long[] performers;
}
