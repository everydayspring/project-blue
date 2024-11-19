package com.sparta.projectblue.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SigninRequestDto {

    @Schema(example = "user654@mail.com")
    @NotBlank
    @Email
    private String email;

    @Schema(example = "abc123?!")
    @NotBlank
    private String password;
}
