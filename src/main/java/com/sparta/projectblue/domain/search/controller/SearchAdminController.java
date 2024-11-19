package com.sparta.projectblue.domain.search.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.sparta.projectblue.config.ApiResponse;
import com.sparta.projectblue.domain.search.document.UserBookingDocument;
import com.sparta.projectblue.domain.search.dto.UserBookingDto;
import com.sparta.projectblue.domain.search.service.UserBookingSearchService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/search")
@RequiredArgsConstructor
@Tag(name = "Admin-Search", description = "검색 API")
public class SearchAdminController {

    private final UserBookingSearchService searchService;

    @GetMapping
    @Operation(summary = "관리자 정보 검색", description = "다양한 조건을 이용해 정보를 검색합니다")
    public ResponseEntity<List<UserBookingDocument>> searchBookings(
            @Parameter(example = "user0") @RequestParam(required = false) String userName,
            @Parameter(example = "Performance0") @RequestParam(required = false)
                    String performanceTitle,
            @Parameter(example = "2024-10-26T22:00:00") @RequestParam(required = false)
                    LocalDateTime bookingDateStart,
            @Parameter(example = "2024-10-26T22:00:00") @RequestParam(required = false)
                    LocalDateTime bookingDateEnd,
            @Parameter(example = "PENDING, COMPLETED, CANCELED") @RequestParam(required = false)
                    String searchReservationStatus,
            @Parameter(example = "1000") @RequestParam(required = false) Long minPaymentAmount,
            @Parameter(example = "200000") @RequestParam(required = false) Long maxPaymentAmount) {

        // 검색 파라미터를 UserBookingDto에 매핑
        UserBookingDto dto =
                new UserBookingDto(
                        null, // reservationId는 검색에 필요하지 않으므로 null로 설정
                        userName,
                        null, // userId는 검색에 필요하지 않으므로 null로 설정
                        performanceTitle,
                        null, // bookingDate는 단일 값이 아닌 범위를 사용하므로 null로 설정
                        null, // paymentAmount는 범위로 검색하므로 null로 설정
                        null, // reservationStatus는 문자열로 처리하여 searchReservationStatus에 매핑
                        null, // paymentId는 검색에 필요하지 않으므로 null로 설정
                        null, // paymentDate는 검색에 필요하지 않으므로 null로 설정
                        bookingDateStart,
                        bookingDateEnd,
                        minPaymentAmount,
                        maxPaymentAmount,
                        searchReservationStatus // 검색 상태 문자열
                        );

        return ResponseEntity.ok(searchService.searchBookings(dto));
    }

    @PostMapping("/sync")
    @Operation(summary = "데이터 동기화", description = "예약 정보를 Elasticsearch와 동기화합니다")
    public ResponseEntity<ApiResponse<String>> syncData() {
        searchService.syncData();
        return ResponseEntity.ok(ApiResponse.success("데이터 동기화가 완료되었습니다."));
    }
}
