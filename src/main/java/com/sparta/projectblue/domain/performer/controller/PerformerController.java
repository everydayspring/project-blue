package com.sparta.projectblue.domain.performer.controller;

import com.sparta.projectblue.domain.performer.dto.GetPerformersResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;
import com.sparta.projectblue.config.ApiResponse;
import com.sparta.projectblue.domain.performer.service.PerformerService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/performers")
@RequiredArgsConstructor
@Tag(name = "Performer", description = "배우 관리 API")
public class PerformerController {

    private final PerformerService performerService;

    // 캐싱 적용 대상 - 배우 단건 조회
    @GetMapping("/{id}")
    @Operation(summary = "배우 단건 조회", description = "배우 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<?>> getPerformer(@PathVariable Long id) {

        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl("no-store");

        return ResponseEntity.ok(ApiResponse.success(performerService.getPerformer(id)));
    }

    // 캐싱 적용 대상 - 배우 다건 조회
    @GetMapping
    @Operation(summary = "배우 다건 조회", description = "배우를 전체 조회합니다.")
    public ResponseEntity<ApiResponse<?>> getPerformers(
            @RequestParam int page,
            @RequestParam int size) {

        GetPerformersResponseDto responseDto = performerService.getPerformers(page, size);

        return ResponseEntity.ok(ApiResponse.success(responseDto));
    }

}
