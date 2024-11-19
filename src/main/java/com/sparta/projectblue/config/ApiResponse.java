package com.sparta.projectblue.config;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    public enum Status {
        SUCCESS,
        ERROR
    }

    private final Status status;
    private final T data;
    private final String message;

    private ApiResponse(Status status, T data, String message) {

        this.status = status;
        this.data = data;
        this.message = message;
    }

    public static <T> ApiResponse<T> success(T data) {

        return new ApiResponse<>(Status.SUCCESS, data, "요청이 성공적으로 처리되었습니다");
    }

    public static ApiResponse<Void> successWithNoContent() {

        return new ApiResponse<>(Status.SUCCESS, null, "요청이 성공적으로 처리되었습니다");
    }

    public static ApiResponse<Void> error(String message) {

        return new ApiResponse<>(Status.ERROR, null, message);
    }
}
