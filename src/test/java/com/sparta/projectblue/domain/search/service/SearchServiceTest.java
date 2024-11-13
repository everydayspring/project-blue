package com.sparta.projectblue.domain.search.service;

import com.sparta.projectblue.domain.hall.entity.Hall;
import com.sparta.projectblue.domain.hall.repository.HallRepository;
import com.sparta.projectblue.domain.performance.dto.GetPerformancesResponseDto;
import com.sparta.projectblue.domain.performance.repository.PerformanceRepository;
import com.sparta.projectblue.domain.performer.entity.Performer;
import com.sparta.projectblue.domain.performer.repository.PerformerRepository;
import com.sparta.projectblue.domain.search.dto.KeywordSearchResponseDto;
import com.sparta.projectblue.domain.search.repository.ESRepository;

import com.sparta.projectblue.domain.search.document.SearchDocument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class SearchServiceTest {
    @Mock
    private PerformanceRepository performanceRepository;

    @Mock
    private ESRepository elasticsearchRepository;

    @Mock
    private HallRepository hallRepository;

    @Mock
    private PerformerRepository performerRepository;

    @InjectMocks
    private SearchService searchService;

    @Test
    void searchFilter_검색조건으로_필터링된_결과가_정상작동한다() {
        // given
        int page = 1;
        int size = 10;
        String performanceNm = "Opera";
        String userSelectDay = "2024-11-07";
        String performer = "John Doe";
        LocalDateTime performanceDay = LocalDateTime.parse(userSelectDay + "T12:00:00");

        Pageable pageable = PageRequest.of(page - 1, size);
        List<GetPerformancesResponseDto> performanceList = List.of(new GetPerformancesResponseDto());
        Page<GetPerformancesResponseDto> expectedPage = new PageImpl<>(performanceList);

        when(performanceRepository.findByCondition(pageable, performanceNm, performanceDay, performer))
                .thenReturn(expectedPage);

        // when
        Page<GetPerformancesResponseDto> result = searchService.searchFilter(page, size, performanceNm, userSelectDay, performer);

        // then
        assertNotNull(result);
        assertEquals(expectedPage, result);
        verify(performanceRepository, times(1)).findByCondition(pageable, performanceNm, performanceDay, performer);
    }

    @Test
    void searchKeyword_키워드로_검색된_결과가_정상작동한다() {
        // given
        String keyword = "Opera";
        Performer performer = new Performer();
        ReflectionTestUtils.setField(performer, "id", 1L);
        Hall hall = new Hall();
        ReflectionTestUtils.setField(hall, "id", 1L);

        SearchDocument searchDocument = new SearchDocument();

        when(performerRepository.findAllByName(keyword)).thenReturn(List.of(performer));
        when(hallRepository.findByNameContaining(keyword)).thenReturn(List.of(hall));
        when(elasticsearchRepository.findByPerformanceTitleContainingOrPerformersPerformerIdInOrHallIdIn(
                keyword, List.of(1L), List.of(1L))).thenReturn(List.of(searchDocument));

        // when
        KeywordSearchResponseDto result = searchService.searchKeyword(keyword);

        // then
        assertNotNull(result);
        assertEquals(1, result.getPerformers().size());
        assertEquals(1, result.getHalls().size());
        assertEquals(1, result.getSearchDocuments().size());
        verify(performerRepository, times(1)).findAllByName(keyword);
        verify(hallRepository, times(1)).findByNameContaining(keyword);
        verify(elasticsearchRepository, times(1)).findByPerformanceTitleContainingOrPerformersPerformerIdInOrHallIdIn(
                keyword, List.of(1L), List.of(1L));

        System.out.println("검색 키워드: " + keyword);
        System.out.println("Expected Performers Count: 1, Actual Performers Count: " + result.getPerformers().size());
        System.out.println("Expected Halls Count: 1, Actual Halls Count: " + result.getHalls().size());
        System.out.println("Expected Search Documents Count: 1, Actual Search Documents Count: " + result.getSearchDocuments().size());
    }

    @Test
    void searchKeyword_빈_키워드일_경우_null_반환한다() {
        // given
        String emptyKeyword = "   ";
        String nullKeyword = null;

        // when
        KeywordSearchResponseDto emptyResult = searchService.searchKeyword(emptyKeyword);
        KeywordSearchResponseDto nullResult = searchService.searchKeyword(nullKeyword);

        // then
        assertNull(emptyResult, "빈 키워드로 검색할 경우 null을 반환해야 합니다.");
        assertNull(nullResult, "null 키워드로 검색할 경우 null을 반환해야 합니다.");

        System.out.println("빈 키워드로 검색 결과: " + emptyResult);
        System.out.println("null 키워드로 검색 결과: " + nullResult);
    }



}
