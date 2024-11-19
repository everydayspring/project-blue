package com.sparta.projectblue.domain.hall.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.sparta.projectblue.config.ApiResponse;
import com.sparta.projectblue.domain.hall.dto.GetHallResponseDto;
import com.sparta.projectblue.domain.hall.dto.GetHallsResponseDto;
import com.sparta.projectblue.domain.hall.service.HallService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/halls")
@Tag(name = "Hall", description = "공연장 API")
public class HallController {

    private final HallService hallService;

    @GetMapping
    @Operation(summary = "공연장 다건 조회")
    public ResponseEntity<ApiResponse<Page<GetHallsResponseDto>>> getHalls(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(ApiResponse.success(hallService.getHalls(page, size)));
    }

    // 캐싱 적용 대상
    @GetMapping("/{id}")
    @Operation(summary = "공연장 단건 조회")
    public ResponseEntity<ApiResponse<GetHallResponseDto>> getHall(@PathVariable Long id) {

        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl("no-store");

        return ResponseEntity.ok(ApiResponse.success(hallService.getHall(id)));
    }
}
