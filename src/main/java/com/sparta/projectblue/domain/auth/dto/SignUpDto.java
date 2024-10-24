package com.sparta.projectblue.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class SignUpDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        @NotBlank
        @Email
        private String email;
        @NotBlank
        private String password;
        @NotBlank
        private String name;
        @NotBlank
        private String userRole;
    }

    @Getter
    public static class Response {
        private final String bearerToken;

        public Response(String bearerToken) {
            this.bearerToken = bearerToken;
        }
    }
}
