package com.sparta.projectblue.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequestDto {

    @NotBlank @Email private String email;

    @NotBlank private String password;

    @NotBlank private String name;

    @NotBlank private String userRole;
}