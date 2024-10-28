package com.sparta.projectblue.domain.hall.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.sparta.projectblue.config.ApiResponse;
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
    public ResponseEntity<ApiResponse<?>> getHalls(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(ApiResponse.success(hallService.getHalls(page, size)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "공연장 단건 조회")
    public ResponseEntity<ApiResponse<?>> getHall(@PathVariable("id") Long id) {

        return ResponseEntity.ok(ApiResponse.success(hallService.getHall(id)));
    }
}
