package com.sparta.projectblue.domain.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.sparta.projectblue.domain.common.enums.ReservationStatus;
import com.sparta.projectblue.domain.reservation.repository.ReservationRepository;
import com.sparta.projectblue.domain.search.document.UserBookingDocument;
import com.sparta.projectblue.domain.search.dto.UserBookingDto;
import com.sparta.projectblue.domain.search.repository.UserBookingEsRepository;

@ExtendWith(MockitoExtension.class)
class UserBookingSearchServiceTest {
    @Mock private UserBookingEsRepository userBookingEsRepository;

    @Mock private ReservationRepository reservationRepository;

    @InjectMocks private UserBookingSearchService userBookingSearchService;

    @Test
    void 예약_검색시_Elasticsearch에서_결과를_반환해야한다_정상작동() {
        // given
        UserBookingDto searchCriteria = new UserBookingDto();
        UserBookingDocument document = new UserBookingDocument();
        when(userBookingEsRepository.searchByCriteria(searchCriteria))
                .thenReturn(List.of(document));

        // when
        List<UserBookingDocument> results = userBookingSearchService.searchBookings(searchCriteria);

        // then
        assertEquals(1, results.size());
        assertEquals(document, results.get(0));
    }

    @Test
    void 사용자_이름으로_검색하면_해당_사용자의_예매내역을_반환한다() {
        // given
        UserBookingDto searchCriteria = new UserBookingDto();
        ReflectionTestUtils.setField(searchCriteria, "userName", "user0");
        UserBookingDocument document =
                new UserBookingDocument(
                        123L,
                        "user0",
                        1L,
                        "musical Opera",
                        LocalDateTime.of(2024, 11, 7, 0, 0),
                        50000L,
                        "COMPLETED",
                        321L,
                        LocalDateTime.of(2024, 11, 7, 0, 0));
        when(userBookingEsRepository.searchByCriteria(searchCriteria))
                .thenReturn(List.of(document));

        // when
        List<UserBookingDocument> results = userBookingSearchService.searchBookings(searchCriteria);

        // then
        assertEquals(1, results.size());
        assertEquals(document, results.get(0));
    }

    @Test
    void 공연_제목으로_검색하면_해당_공연의_예매내역을_반환한다() {
        // given
        UserBookingDto searchCriteria = new UserBookingDto();
        ReflectionTestUtils.setField(searchCriteria, "performanceTitle", "musical Opera");

        UserBookingDocument document =
                new UserBookingDocument(
                        123L,
                        "user0",
                        1L,
                        "musical Opera",
                        LocalDateTime.of(2024, 11, 7, 0, 0),
                        50000L,
                        "COMPLETED",
                        321L,
                        LocalDateTime.of(2024, 11, 7, 0, 0));
        when(userBookingEsRepository.searchByCriteria(searchCriteria))
                .thenReturn(List.of(document));

        // when
        List<UserBookingDocument> results = userBookingSearchService.searchBookings(searchCriteria);

        // then
        assertEquals(1, results.size());
        assertEquals(document, results.get(0));
    }

    @Test
    void 예약_날짜_범위로_검색하면_해당_기간의_예매내역을_반환한다() {
        // given
        UserBookingDto searchCriteria = new UserBookingDto();
        ReflectionTestUtils.setField(
                searchCriteria, "bookingDateStart", LocalDateTime.of(2024, 11, 1, 0, 0));
        ReflectionTestUtils.setField(
                searchCriteria, "bookingDateEnd", LocalDateTime.of(2024, 11, 30, 23, 59));

        UserBookingDocument document =
                new UserBookingDocument(
                        123L,
                        "user0",
                        1L,
                        "musical Opera",
                        LocalDateTime.of(2024, 11, 7, 0, 0),
                        50000L,
                        "COMPLETED",
                        321L,
                        LocalDateTime.of(2024, 11, 7, 0, 0));
        when(userBookingEsRepository.searchByCriteria(searchCriteria))
                .thenReturn(List.of(document));

        // when
        List<UserBookingDocument> results = userBookingSearchService.searchBookings(searchCriteria);

        // then
        assertEquals(1, results.size());
        assertEquals(document, results.get(0));
    }

    @Test
    void 결제_금액_범위로_검색하면_해당_범위의_예매내역을_반환한다() {
        // given
        UserBookingDto searchCriteria = new UserBookingDto();
        ReflectionTestUtils.setField(searchCriteria, "minPaymentAmount", 30000L);
        ReflectionTestUtils.setField(searchCriteria, "maxPaymentAmount", 60000L);

        UserBookingDocument document =
                new UserBookingDocument(
                        123L,
                        "user0",
                        1L,
                        "musical Opera",
                        LocalDateTime.of(2024, 11, 7, 0, 0),
                        50000L,
                        "COMPLETED",
                        321L,
                        LocalDateTime.of(2024, 11, 7, 0, 0));
        when(userBookingEsRepository.searchByCriteria(searchCriteria))
                .thenReturn(List.of(document));

        // when
        List<UserBookingDocument> results = userBookingSearchService.searchBookings(searchCriteria);

        // then
        assertEquals(1, results.size());
        assertEquals(document, results.get(0));
    }

    @Test
    void 예약_상태로_검색하면_해당_상태의_예매내역을_반환한다() {
        // given
        UserBookingDto searchCriteria = new UserBookingDto();
        ReflectionTestUtils.setField(searchCriteria, "searchReservationStatus", "COMPLETED");

        UserBookingDocument document =
                new UserBookingDocument(
                        123L,
                        "user0",
                        1L,
                        "musical Opera",
                        LocalDateTime.of(2024, 11, 7, 0, 0),
                        50000L,
                        "COMPLETED",
                        321L,
                        LocalDateTime.of(2024, 11, 7, 0, 0));
        when(userBookingEsRepository.searchByCriteria(searchCriteria))
                .thenReturn(List.of(document));

        // when
        List<UserBookingDocument> results = userBookingSearchService.searchBookings(searchCriteria);

        // then
        assertEquals(1, results.size());
        assertEquals(document, results.get(0));
    }

    @Test
    void 최소_예약_날짜만_설정하면_해당_날짜_이후의_예매내역을_반환한다() {
        // given
        UserBookingDto searchCriteria = new UserBookingDto();
        ReflectionTestUtils.setField(
                searchCriteria, "bookingDateStart", LocalDateTime.of(2024, 11, 1, 0, 0));

        UserBookingDocument document =
                new UserBookingDocument(
                        123L,
                        "user0",
                        1L,
                        "musical Opera",
                        LocalDateTime.of(2024, 11, 7, 0, 0),
                        50000L,
                        "COMPLETED",
                        321L,
                        LocalDateTime.of(2024, 11, 7, 0, 0));
        when(userBookingEsRepository.searchByCriteria(searchCriteria))
                .thenReturn(List.of(document));

        // when
        List<UserBookingDocument> results = userBookingSearchService.searchBookings(searchCriteria);

        // then
        assertEquals(1, results.size());
        assertEquals(document, results.get(0));
    }

    @Test
    void 최대_예약_날짜만_설정하면_해당_날짜_이전의_예매내역을_반환한다() {
        // given
        UserBookingDto searchCriteria = new UserBookingDto();
        ReflectionTestUtils.setField(
                searchCriteria, "bookingDateEnd", LocalDateTime.of(2024, 11, 30, 23, 59));

        UserBookingDocument document =
                new UserBookingDocument(
                        123L,
                        "user0",
                        1L,
                        "musical Opera",
                        LocalDateTime.of(2024, 11, 7, 0, 0),
                        50000L,
                        "COMPLETED",
                        321L,
                        LocalDateTime.of(2024, 11, 7, 0, 0));
        when(userBookingEsRepository.searchByCriteria(searchCriteria))
                .thenReturn(List.of(document));

        // when
        List<UserBookingDocument> results = userBookingSearchService.searchBookings(searchCriteria);

        // then
        assertEquals(1, results.size());
        assertEquals(document, results.get(0));
    }

    @Test
    void 최소_결제_금액만_설정하면_해당_금액_이상의_예매내역을_반환한다() {
        // given
        UserBookingDto searchCriteria = new UserBookingDto();
        ReflectionTestUtils.setField(searchCriteria, "minPaymentAmount", 30000L);

        UserBookingDocument document =
                new UserBookingDocument(
                        123L,
                        "user0",
                        1L,
                        "musical Opera",
                        LocalDateTime.of(2024, 11, 7, 0, 0),
                        50000L,
                        "COMPLETED",
                        321L,
                        LocalDateTime.of(2024, 11, 7, 0, 0));
        when(userBookingEsRepository.searchByCriteria(searchCriteria))
                .thenReturn(List.of(document));

        // when
        List<UserBookingDocument> results = userBookingSearchService.searchBookings(searchCriteria);

        // then
        assertEquals(1, results.size());
        assertEquals(document, results.get(0));
    }

    @Test
    void 최대_결제_금액만_설정하면_해당_금액_이하의_예매내역을_반환한다() {
        // given
        UserBookingDto searchCriteria = new UserBookingDto();
        ReflectionTestUtils.setField(searchCriteria, "maxPaymentAmount", 60000L);

        UserBookingDocument document =
                new UserBookingDocument(
                        123L,
                        "user0",
                        1L,
                        "musical Opera",
                        LocalDateTime.of(2024, 11, 7, 0, 0),
                        50000L,
                        "COMPLETED",
                        321L,
                        LocalDateTime.of(2024, 11, 7, 0, 0));
        when(userBookingEsRepository.searchByCriteria(searchCriteria))
                .thenReturn(List.of(document));

        // when
        List<UserBookingDocument> results = userBookingSearchService.searchBookings(searchCriteria);

        // then
        assertEquals(1, results.size());
        assertEquals(document, results.get(0));
    }

    @Test
    void syncData_호출시_데이터베이스와_Elasticsearch_동기화가_정상작동한다() {
        // given
        UserBookingDto dto =
                new UserBookingDto(
                        123L,
                        "user0",
                        1L,
                        "musical Opera",
                        LocalDateTime.of(2024, 11, 7, 0, 0),
                        50000L,
                        ReservationStatus.COMPLETED,
                        321L,
                        LocalDateTime.of(2024, 11, 7, 0, 0));

        UserBookingDocument expectedDocument =
                new UserBookingDocument(
                        123L,
                        "user0",
                        1L,
                        "musical Opera",
                        LocalDateTime.of(2024, 11, 7, 0, 0),
                        50000L,
                        "COMPLETED",
                        321L,
                        LocalDateTime.of(2024, 11, 7, 0, 0));

        when(reservationRepository.findUserBookingData()).thenReturn(List.of(dto));

        // when
        userBookingSearchService.syncData();

        // then
        ArgumentCaptor<List<UserBookingDocument>> captor = ArgumentCaptor.forClass(List.class);
        verify(userBookingEsRepository).saveAll(captor.capture());

        // 캡처한 결과에서 첫 번째 document 확인
        UserBookingDocument actualDocument = captor.getValue().get(0);
        assertEquals(expectedDocument.getReservationId(), actualDocument.getReservationId());
        assertEquals(expectedDocument.getUserName(), actualDocument.getUserName());
        assertEquals(expectedDocument.getUserId(), actualDocument.getUserId());
        assertEquals(expectedDocument.getPerformanceTitle(), actualDocument.getPerformanceTitle());
        assertEquals(expectedDocument.getBookingDate(), actualDocument.getBookingDate());
        assertEquals(expectedDocument.getPaymentAmount(), actualDocument.getPaymentAmount());
        assertEquals(
                expectedDocument.getReservationStatus(), actualDocument.getReservationStatus());
        assertEquals(expectedDocument.getPaymentId(), actualDocument.getPaymentId());
        assertEquals(expectedDocument.getPaymentDate(), actualDocument.getPaymentDate());
    }
}
