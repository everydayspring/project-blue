package com.sparta.projectblue.config;

import jakarta.security.auth.message.AuthException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.sparta.projectblue.domain.common.exception.PaymentException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidRequestException(
            IllegalArgumentException ex) {

        HttpStatus status = HttpStatus.BAD_REQUEST;

        return getErrorResponse(status, ex.getMessage());
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthException(AuthException ex) {

        HttpStatus status = HttpStatus.UNAUTHORIZED;

        return getErrorResponse(status, ex.getMessage());
    }

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<ApiResponse<Void>> handlePaymentException(AuthException ex) {

        HttpStatus status = HttpStatus.PAYMENT_REQUIRED;

        return getErrorResponse(status, ex.getMessage());
    }

    public ResponseEntity<ApiResponse<Void>> getErrorResponse(HttpStatus status, String message) {

        return ResponseEntity.status(status).body(ApiResponse.error("오류가 발생했습니다." + message));
    }
}
