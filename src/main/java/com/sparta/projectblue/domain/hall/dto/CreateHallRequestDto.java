package com.sparta.projectblue.domain.hall.dto;

import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateHallRequestDto {

    @Schema(example = "한화생명이글스파크")
    @NotNull
    private String name;

    @Schema(example = "대전광역시 중구 대종로 373")
    @NotNull
    private String address;

    @Schema(example = "13000")
    @NotNull
    private Integer seats;
}
