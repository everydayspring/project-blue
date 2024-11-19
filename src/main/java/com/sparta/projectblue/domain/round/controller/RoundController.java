package com.sparta.projectblue.domain.round.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.projectblue.config.ApiResponse;
import com.sparta.projectblue.domain.round.dto.GetRoundAvailableSeatsResponseDto;
import com.sparta.projectblue.domain.round.service.RoundService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/rounds")
@RequiredArgsConstructor
@Tag(name = "Round", description = "회차 관련 API")
public class RoundController {

    private final RoundService roundService;

    @GetMapping("/{id}")
    @Operation(summary = "예매가능 좌석 조회", description = "입력 회차의 예매 가능 좌석을 모두 출력")
    public ResponseEntity<ApiResponse<GetRoundAvailableSeatsResponseDto>> getAvailableSeats(
            @PathVariable Long id) {

        return ResponseEntity.ok(ApiResponse.success(roundService.getAvailableSeats(id)));
    }
}
