package com.sparta.projectblue.domain.performer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePerformerRequestDto {

    @Schema(example = "홍광호")
    @NotBlank
    private String name;

    @Schema(example = "1982-04-06")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$")
    private String birth;

    @Schema(example = "대한민국")
    private String nation;
}
