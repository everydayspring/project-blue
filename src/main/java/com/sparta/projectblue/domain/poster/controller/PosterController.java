package com.sparta.projectblue.domain.poster.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sparta.projectblue.config.ApiResponse;
import com.sparta.projectblue.domain.common.dto.AuthUser;
import com.sparta.projectblue.domain.poster.dto.UpdatePosterResponseDto;
import com.sparta.projectblue.domain.poster.service.PosterService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@Tag(name = "Admin-Poster", description = "포스터 CRUD API")
public class PosterController {

    private final PosterService posterService;

    @Operation(summary = "포스터 수정", description = "파일 첨부로 포스트맨에서 테스트합니다")
    @PutMapping("/admin/posters/{id}")
    public ResponseEntity<ApiResponse<UpdatePosterResponseDto>> update(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long id,
            @RequestPart("file") MultipartFile posterFile) {

        return ResponseEntity.ok(
                ApiResponse.success(posterService.update(authUser, id, posterFile)));
    }
}
