package com.sparta.projectblue.domain.search.repository;

import java.util.List;

import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
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

    // 필드 이름 상수 정의
    private static final String FIELD_BOOKING_DATE = "bookingDate";
    private static final String FIELD_USER_NAME = "userName";
    private static final String FIELD_PERFORMANCE_TITLE = "performanceTitle";
    private static final String FIELD_PAYMENT_AMOUNT = "paymentAmount";
    private static final String FIELD_RESERVATION_STATUS = "reservationStatus";

    @Override
    public List<UserBookingDocument> searchByCriteria(UserBookingDto request) {

        Criteria criteria = new Criteria();

        // 사용자 이름 조건 추가
        if (request.getUserName() != null && !request.getUserName().isEmpty()) {
            criteria = criteria.and(FIELD_USER_NAME).is(request.getUserName());
        }

        // 공연 제목 조건 추가
        if (request.getPerformanceTitle() != null && !request.getPerformanceTitle().isEmpty()) {
            criteria = criteria.and(FIELD_PERFORMANCE_TITLE).is(request.getPerformanceTitle());
        }

        // 예약 날짜 범위 조건 추가
        if (request.getBookingDateStart() != null && request.getBookingDateEnd() != null) {
            criteria =
                    criteria.and(FIELD_BOOKING_DATE)
                            .between(request.getBookingDateStart(), request.getBookingDateEnd());
        } else if (request.getBookingDateStart() != null) {
            criteria =
                    criteria.and(FIELD_BOOKING_DATE)
                            .greaterThanEqual(request.getBookingDateStart());
        } else if (request.getBookingDateEnd() != null) {
            criteria = criteria.and(FIELD_BOOKING_DATE).lessThanEqual(request.getBookingDateEnd());
        }

        // 결제 금액 범위 조건 추가
        if (request.getMinPaymentAmount() != null && request.getMaxPaymentAmount() != null) {
            criteria =
                    criteria.and(FIELD_PAYMENT_AMOUNT)
                            .between(request.getMinPaymentAmount(), request.getMaxPaymentAmount());
        } else if (request.getMinPaymentAmount() != null) {
            criteria =
                    criteria.and(FIELD_PAYMENT_AMOUNT)
                            .greaterThanEqual(request.getMinPaymentAmount());
        } else if (request.getMaxPaymentAmount() != null) {
            criteria =
                    criteria.and(FIELD_PAYMENT_AMOUNT).lessThanEqual(request.getMaxPaymentAmount());
        }

        // 예약 상태 조건 추가
        if (request.getSearchReservationStatus() != null
                && !request.getSearchReservationStatus().isEmpty()) {
            criteria =
                    criteria.and(FIELD_RESERVATION_STATUS).is(request.getSearchReservationStatus());
        }

        CriteriaQuery query = new CriteriaQuery(criteria);

        return elasticsearchOperations
                .search(query, UserBookingDocument.class)
                .getSearchHits()
                .stream()
                .map(SearchHit::getContent)
                .toList();
    }
}
