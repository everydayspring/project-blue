package com.sparta.projectblue.domain.hall.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateHallRequestDto {

    @Schema(example = "베이스볼 드림파크")
    private String name;

    @Schema(example = "대전광역시 중구 대종로 373")
    private String address;

    @Schema(example = "20007")
    private Integer seats;
}
