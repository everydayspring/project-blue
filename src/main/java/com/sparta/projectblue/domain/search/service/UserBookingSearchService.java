package com.sparta.projectblue.domain.search.service;

import java.util.List;
import java.util.stream.Collectors;

import com.sparta.projectblue.domain.reservation.repository.ReservationRepository;
import org.springframework.stereotype.Service;

import com.sparta.projectblue.domain.search.document.UserBookingDocument;
import com.sparta.projectblue.domain.search.dto.UserBookingDto;
import com.sparta.projectblue.domain.search.repository.UserBookingEsRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserBookingSearchService {
    private final UserBookingEsRepository userBookingEsRepository;
    private final ReservationRepository reservationRepository;

    @Transactional
    public void syncData() {
        // 데이터베이스에서 필요한 데이터 조회
        List<UserBookingDocument> documents = reservationRepository.findUserBookingData().stream()
                .map(this::convertToDocument)
                .collect(Collectors.toList());

        // Elasticsearch 인덱스에 동기화
        userBookingEsRepository.saveAll(documents);
    }

    private UserBookingDocument convertToDocument(UserBookingDto dto) {
        return new UserBookingDocument(
                dto.getReservationId(),
                dto.getUserName(),
                dto.getUserId(),
                dto.getPerformanceTitle(),
                dto.getBookingDate(),
                dto.getPaymentAmount(),
                dto.getReservationStatus().name(), // Enum을 문자열로 변환하여 저장
                dto.getPaymentId(),
                dto.getPaymentDate()
        );
    }
    public List<UserBookingDocument> searchBookings(UserBookingDto searchCriteria) {
        return userBookingEsRepository.searchByCriteria(searchCriteria);
    }

}
