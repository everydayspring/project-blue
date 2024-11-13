package com.sparta.projectblue.domain.search.repository;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Repository;

import com.sparta.projectblue.domain.search.document.UserBookingDocument;
import com.sparta.projectblue.domain.search.dto.UserBookingDto;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserBookingEsRepositoryImpl implements UserBookingEsCustomRepository {

    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public List<UserBookingDocument> searchByCriteria(UserBookingDto request) {
        Criteria criteria = new Criteria();

        // 사용자 이름 조건 추가
        if (request.getUserName() != null && !request.getUserName().isEmpty()) {
            criteria = criteria.and("userName").is(request.getUserName());
        }

        // 공연 제목 조건 추가
        if (request.getPerformanceTitle() != null && !request.getPerformanceTitle().isEmpty()) {
            criteria = criteria.and("performanceTitle").is(request.getPerformanceTitle());
        }

        // 예약 날짜 범위 조건 추가
        if (request.getBookingDateStart() != null && request.getBookingDateEnd() != null) {
            criteria = criteria.and("bookingDate")
                    .between(request.getBookingDateStart(), request.getBookingDateEnd());
        } else if (request.getBookingDateStart() != null) {
            criteria = criteria.and("bookingDate").greaterThanEqual(request.getBookingDateStart());
        } else if (request.getBookingDateEnd() != null) {
            criteria = criteria.and("bookingDate").lessThanEqual(request.getBookingDateEnd());
        }

        // 결제 금액 범위 조건 추가
        if (request.getMinPaymentAmount() != null && request.getMaxPaymentAmount() != null) {
            criteria = criteria.and("paymentAmount")
                    .between(request.getMinPaymentAmount(), request.getMaxPaymentAmount());
        } else if (request.getMinPaymentAmount() != null) {
            criteria = criteria.and("paymentAmount").greaterThanEqual(request.getMinPaymentAmount());
        } else if (request.getMaxPaymentAmount() != null) {
            criteria = criteria.and("paymentAmount").lessThanEqual(request.getMaxPaymentAmount());
        }

        // 예약 상태 조건 추가
        if (request.getSearchReservationStatus() != null && !request.getSearchReservationStatus().isEmpty()) {
            criteria = criteria.and("reservationStatus").is(request.getSearchReservationStatus());
        }

        CriteriaQuery query = new CriteriaQuery(criteria);

        return elasticsearchOperations
                .search(query, UserBookingDocument.class)
                .getSearchHits()
                .stream()
                .map(hit -> hit.getContent())
                .collect(Collectors.toList());
    }
}
