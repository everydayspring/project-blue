package com.sparta.projectblue.domain.performance.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.sparta.projectblue.domain.common.dto.AuthUser;
import com.sparta.projectblue.domain.common.enums.Category;
import com.sparta.projectblue.domain.common.enums.UserRole;
import com.sparta.projectblue.domain.hall.repository.HallRepository;
import com.sparta.projectblue.domain.performance.dto.CreatePerformanceRequestDto;
import com.sparta.projectblue.domain.performance.dto.CreatePerformanceResponseDto;
import com.sparta.projectblue.domain.performance.dto.UpdatePerformanceRequestDto;
import com.sparta.projectblue.domain.performance.dto.UpdatePerformanceResponseDto;
import com.sparta.projectblue.domain.performance.entity.Performance;
import com.sparta.projectblue.domain.performance.repository.PerformanceRepository;
import com.sparta.projectblue.domain.performer.entity.Performer;
import com.sparta.projectblue.domain.performer.repository.PerformerRepository;
import com.sparta.projectblue.domain.performerperformance.entity.PerformerPerformance;
import com.sparta.projectblue.domain.performerperformance.repository.PerformerPerformanceRepository;
import com.sparta.projectblue.domain.poster.entity.Poster;
import com.sparta.projectblue.domain.poster.repository.PosterRepository;
import com.sparta.projectblue.domain.round.repository.RoundRepository;
import com.sparta.projectblue.domain.user.entity.User;
import com.sparta.projectblue.domain.user.repository.UserRepository;

@ExtendWith(SpringExtension.class)
class PerformanceAdminServiceTest {

    @InjectMocks private PerformanceAdminService performanceAdminService;

    @Mock private HallRepository hallRepository;

    @Mock private PerformerRepository performerRepository;

    @Mock private PerformanceRepository performanceRepository;

    @Mock private UserRepository userRepository;

    @Mock private PerformerPerformanceRepository performerPerformanceRepository;

    @Mock private PosterRepository posterRepository;

    @Mock private RoundRepository roundRepository;

    @Mock private AmazonS3 amazonS3;

    @Test
    void 공연_생성_성공_테스트() throws Exception {
        // Given
        AuthUser authUser = new AuthUser(1L, "a@a.a", "aaaaaa1!", UserRole.ROLE_ADMIN);
        User user = new User();
        CreatePerformanceRequestDto requestDto =
                new CreatePerformanceRequestDto(
                        "공연 Test",
                        "2025-11-10",
                        "2025-11-15",
                        5000L,
                        "CONCERT",
                        "공연 생성 테스트",
                        1L,
                        100,
                        new Long[] {1L, 2L});

        MockMultipartFile posterFile =
                new MockMultipartFile("poster_name", "poster.png", "image/png", new byte[1024]);

        // When
        ReflectionTestUtils.setField(user, "userRole", UserRole.ROLE_ADMIN);
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(user));
        when(hallRepository.existsById(anyLong())).thenReturn(true);
        when(performerRepository.existsById(any())).thenReturn(true);
        when(performerPerformanceRepository.save(any(PerformerPerformance.class)))
                .thenReturn(new PerformerPerformance());

        Performance savedPerformance =
                new Performance(
                        1L,
                        "공연 테스트",
                        LocalDateTime.now().plusDays(2),
                        LocalDateTime.now().plusDays(5),
                        5000L,
                        Category.CONCERT,
                        "공연 생성 테스트중",
                        120);
        when(performanceRepository.save(any(Performance.class))).thenReturn(savedPerformance);

        Poster poster = new Poster();
        when(posterRepository.save(any(Poster.class))).thenReturn(poster);

        doReturn(null).when(amazonS3).putObject(any(PutObjectRequest.class));
        ReflectionTestUtils.setField(performanceAdminService, "bucket", "test-bucket");
        given(amazonS3.getUrl(anyString(), anyString()))
                .willReturn(new java.net.URL("https://example.com/poster.jpg"));

        CreatePerformanceResponseDto responseDto =
                performanceAdminService.create(authUser, requestDto, posterFile);

        // Then
        assertNotNull(responseDto);
        assertEquals("공연 테스트", responseDto.getTitle());
    }

    @Test
    void 파일_업로드_실패로_예외가_발생() throws IOException {
        // Given
        AuthUser authUser = new AuthUser(1L, "a@a.a", "aaaaaa1!", UserRole.ROLE_ADMIN);
        User user = new User();
        CreatePerformanceRequestDto requestDto =
                new CreatePerformanceRequestDto(
                        "공연 Test",
                        "2025-11-10",
                        "2025-11-15",
                        5000L,
                        "CONCERT",
                        "공연 생성 테스트",
                        1L,
                        100,
                        new Long[] {1L, 2L});

        MultipartFile posterFile = mock(MultipartFile.class);

        // When
        ReflectionTestUtils.setField(user, "userRole", UserRole.ROLE_ADMIN);
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(user));
        when(hallRepository.existsById(anyLong())).thenReturn(true);
        when(performerRepository.existsById(any())).thenReturn(true);
        when(performerPerformanceRepository.save(any(PerformerPerformance.class)))
                .thenReturn(new PerformerPerformance());

        Performance savedPerformance =
                new Performance(
                        1L,
                        "공연 테스트",
                        LocalDateTime.now().plusDays(2),
                        LocalDateTime.now().plusDays(5),
                        5000L,
                        Category.CONCERT,
                        "공연 생성 테스트중",
                        120);
        when(performanceRepository.save(any(Performance.class))).thenReturn(savedPerformance);

        doReturn(null).when(amazonS3).putObject(any(PutObjectRequest.class));

        when(posterFile.getOriginalFilename()).thenReturn("test.png");
        when(posterFile.getInputStream()).thenThrow(new IOException());

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> {
                            performanceAdminService.create(authUser, requestDto, posterFile);
                        });

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        assertEquals("500 INTERNAL_SERVER_ERROR \"파일 업로드에 실패했습니다.\"", exception.getMessage());
    }

    @Test
    void DB에서의_관리자_권한이_없어서_예외가_발생() {
        // Given
        AuthUser authUser = new AuthUser(1L, "a@a.a", "aaaaaa1!", UserRole.ROLE_ADMIN);
        CreatePerformanceRequestDto requestDto = new CreatePerformanceRequestDto();
        User user = new User("a@a.a", "test", "aaaaaa1!", UserRole.ROLE_USER);
        ReflectionTestUtils.setField(user, "id", 1L);

        // When
        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> {
                            performanceAdminService.create(authUser, requestDto, null);
                        });

        // Then
        assertEquals("관리자만 접근할 수 있습니다.", exception.getMessage());
    }

    @Test
    void 공연장이_없어서_예외가_발생() {
        // Given
        AuthUser authUser = new AuthUser(1L, "a@a.a", "aaaaaa1!", UserRole.ROLE_ADMIN);
        User user = new User();
        ReflectionTestUtils.setField(user, "userRole", UserRole.ROLE_ADMIN);
        CreatePerformanceRequestDto requestDto = new CreatePerformanceRequestDto();
        Long hallId = 999L;

        // When
        when(hallRepository.existsById(hallId)).thenReturn(false);
        when(userRepository.findById(any())).thenReturn(Optional.of(user));

        // Then
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> {
                            performanceAdminService.create(authUser, requestDto, null);
                        });

        assertEquals("존재하지 않는 공연장입니다.", exception.getMessage());
    }

    @Test
    void 시작일이_현재보다_이전이라_예외가_발생() {
        // Given
        AuthUser authUser = new AuthUser(1L, "a@a.a", "aaaaaa1!", UserRole.ROLE_ADMIN);
        User user = new User();
        ReflectionTestUtils.setField(user, "userRole", UserRole.ROLE_ADMIN);
        CreatePerformanceRequestDto requestDto = new CreatePerformanceRequestDto();
        ReflectionTestUtils.setField(requestDto, "startDate", "2023-11-01");

        // When
        when(hallRepository.existsById((any()))).thenReturn(true);
        when(userRepository.findById(any())).thenReturn(Optional.of(user));

        // Then
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> {
                            performanceAdminService.create(authUser, requestDto, null);
                        });

        assertEquals("시작일이 현재보다 이전일 수 없습니다.", exception.getMessage());
    }

    @Test
    void 종료일이_현재보다_이전이라_예외가_발생() {
        // Given
        AuthUser authUser = new AuthUser(1L, "a@a.a", "aaaaaa1!", UserRole.ROLE_ADMIN);
        User user = new User();
        ReflectionTestUtils.setField(user, "userRole", UserRole.ROLE_ADMIN);
        CreatePerformanceRequestDto requestDto = new CreatePerformanceRequestDto();
        ReflectionTestUtils.setField(requestDto, "startDate", "2025-11-10");
        ReflectionTestUtils.setField(requestDto, "endDate", "2023-11-01");
        // When
        when(hallRepository.existsById((any()))).thenReturn(true);
        when(userRepository.findById(any())).thenReturn(Optional.of(user));

        // Then
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> {
                            performanceAdminService.create(authUser, requestDto, null);
                        });

        assertEquals("종료일이 현재보다 이전일 수 없습니다.", exception.getMessage());
    }

    @Test
    void 종료일이_시작일보다_이전이라_예외가_발생() {
        // Given
        AuthUser authUser = new AuthUser(1L, "a@a.a", "aaaaaa1!", UserRole.ROLE_ADMIN);
        User user = new User();
        ReflectionTestUtils.setField(user, "userRole", UserRole.ROLE_ADMIN);
        CreatePerformanceRequestDto requestDto = new CreatePerformanceRequestDto();
        ReflectionTestUtils.setField(requestDto, "startDate", "2025-11-15");
        ReflectionTestUtils.setField(requestDto, "endDate", "2025-11-14");

        // When
        when(hallRepository.existsById((any()))).thenReturn(true);
        when(userRepository.findById(any())).thenReturn(Optional.of(user));

        // Then
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> {
                            performanceAdminService.create(authUser, requestDto, null);
                        });

        assertEquals("종료일이 시작일보다 빠를 수 없습니다.", exception.getMessage());
    }

    @Test
    void 공연시간이_0이라서_예외가_발생() {
        // Given
        AuthUser authUser = new AuthUser(1L, "a@a.a", "aaaaaa1!", UserRole.ROLE_ADMIN);
        User user = new User();
        ReflectionTestUtils.setField(user, "userRole", UserRole.ROLE_ADMIN);
        CreatePerformanceRequestDto requestDto = new CreatePerformanceRequestDto();
        ReflectionTestUtils.setField(requestDto, "startDate", "2025-11-15");
        ReflectionTestUtils.setField(requestDto, "endDate", "2025-11-24");
        ReflectionTestUtils.setField(requestDto, "duration", 0);
        // When
        when(hallRepository.existsById((any()))).thenReturn(true);
        when(userRepository.findById(any())).thenReturn(Optional.of(user));

        // Then
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> {
                            performanceAdminService.create(authUser, requestDto, null);
                        });

        assertEquals("공연시간을 입력해주세요.", exception.getMessage());
    }

    @Test
    void 잘못된_형식의_파일_이름으로_예외발생() {

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> {
                            performanceAdminService.createFileName("test");
                        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("400 BAD_REQUEST \"잘못된 형식의 파일(test) 입니다.\"", exception.getMessage());
    }

    @Test
    void 등록하려는_출연자가_존재하지_않아_예외가_발생() {
        AuthUser authUser = new AuthUser(1L, "a@a.a", "aaaaaa1!", UserRole.ROLE_ADMIN);
        User user = new User();
        CreatePerformanceRequestDto requestDto =
                new CreatePerformanceRequestDto(
                        "공연 Test",
                        "2025-11-10",
                        "2025-11-15",
                        5000L,
                        "CONCERT",
                        "공연 생성 테스트",
                        1L,
                        100,
                        new Long[] {1L, 2L});

        MultipartFile posterFile = mock(MultipartFile.class);

        // When
        ReflectionTestUtils.setField(user, "userRole", UserRole.ROLE_ADMIN);
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(user));
        when(hallRepository.existsById(anyLong())).thenReturn(true);
        when(performerRepository.existsById(any())).thenReturn(true);
        when(performerPerformanceRepository.save(any(PerformerPerformance.class)))
                .thenReturn(new PerformerPerformance());

        Performance savedPerformance = new Performance();
        when(performanceRepository.save(any(Performance.class))).thenReturn(savedPerformance);
        when(performerRepository.existsById(any())).thenReturn(false);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> {
                            performanceAdminService.create(authUser, requestDto, posterFile);
                        });

        assertEquals("존재하지 않는 출연자입니다.", exception.getMessage());
    }

    @Test
    void 수정하려는_공연이_존재하지_않아_예외가_발생() {
        AuthUser authUser = new AuthUser(1L, "a@a.a", "aaaaaa1!", UserRole.ROLE_ADMIN);
        Long performanceId = 1L;
        UpdatePerformanceRequestDto requestDto = new UpdatePerformanceRequestDto();
        User user = new User();
        ReflectionTestUtils.setField(user, "userRole", UserRole.ROLE_ADMIN);

        // When
        when(performanceRepository.findById(any())).thenReturn(Optional.empty());
        when(userRepository.findById(any())).thenReturn(Optional.of(user));

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> {
                            performanceAdminService.update(authUser, performanceId, requestDto);
                        });

        assertEquals("존재하지 않는 공연입니다.", exception.getMessage());
    }

    @Test
    void 유저가_존재하지_않아_예외가_발생() {
        AuthUser authUser = new AuthUser(1L, "a@a.a", "aaaaaa1!", UserRole.ROLE_ADMIN);
        Long performanceId = 1L;
        UpdatePerformanceRequestDto requestDto = new UpdatePerformanceRequestDto();

        // When
        when(userRepository.findById(any())).thenReturn(Optional.empty());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> {
                            performanceAdminService.update(authUser, performanceId, requestDto);
                        });

        assertEquals("존재하지 않는 유저입니다.", exception.getMessage());
    }

    @Test
    void 삭제하려는_출연자가_없어서_예외가_발생() {
        AuthUser authUser = new AuthUser(1L, "a@a.a", "aaaaaa1!", UserRole.ROLE_ADMIN);
        Long performanceId = 1L;
        UpdatePerformanceRequestDto requestDto =
                new UpdatePerformanceRequestDto(
                        "수정테스트",
                        "2024-11-10",
                        "2024-11-15",
                        5000L,
                        Category.CONCERT,
                        "수정 테스트코드",
                        1L,
                        200,
                        new Long[] {1L, 2L});
        User user = new User();
        ReflectionTestUtils.setField(user, "userRole", UserRole.ROLE_ADMIN);

        // When
        when(performanceRepository.findById(anyLong())).thenReturn(Optional.of(new Performance()));
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(user));
        when(performerPerformanceRepository.findAllByPerformanceId(any())).thenReturn(null);

        assertThrows(
                NullPointerException.class,
                () -> {
                    performanceAdminService.update(authUser, performanceId, requestDto);
                });
    }

    @Test
    void 삭제하려는_출연자가_List가_비어있어서_예외가_발생() {
        AuthUser authUser = new AuthUser(1L, "a@a.a", "aaaaaa1!", UserRole.ROLE_ADMIN);
        Long performanceId = 1L;
        UpdatePerformanceRequestDto requestDto =
                new UpdatePerformanceRequestDto(
                        "수정테스트",
                        "2024-11-10",
                        "2024-11-15",
                        5000L,
                        Category.CONCERT,
                        "수정 테스트코드",
                        1L,
                        200,
                        new Long[] {1L, 2L});
        User user = new User();
        ReflectionTestUtils.setField(user, "userRole", UserRole.ROLE_ADMIN);

        List<PerformerPerformance> performerPerformanceList = List.of();

        // When
        when(performanceRepository.findById(anyLong())).thenReturn(Optional.of(new Performance()));
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(user));
        when(performerPerformanceRepository.findAllByPerformanceId(any()))
                .thenReturn(performerPerformanceList);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> {
                            performanceAdminService.update(authUser, performanceId, requestDto);
                        });

        assertEquals("존재하지 않는 출연자입니다.", exception.getMessage());
    }

    @Test
    void 공연_정보_수정_성공() {
        AuthUser authUser = new AuthUser(1L, "a@a.a", "aaaaaa1!", UserRole.ROLE_ADMIN);
        Long performanceId = 1L;
        UpdatePerformanceRequestDto requestDto =
                new UpdatePerformanceRequestDto(
                        "수정테스트",
                        "2024-11-10",
                        "2024-11-15",
                        5000L,
                        Category.CONCERT,
                        "수정 테스트코드",
                        1L,
                        200,
                        new Long[] {1L, 2L});
        User user = new User();
        ReflectionTestUtils.setField(user, "userRole", UserRole.ROLE_ADMIN);
        List<PerformerPerformance> performerPerformances =
                List.of(new PerformerPerformance(), new PerformerPerformance());

        Performance performance =
                new Performance(
                        1L,
                        "수정 테스트",
                        LocalDateTime.now().plusDays(2),
                        LocalDateTime.now().plusDays(5),
                        5000L,
                        Category.CONCERT,
                        "수정 테스트중",
                        100);

        // When
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(user));
        when(performanceRepository.findById(anyLong())).thenReturn(Optional.of(performance));
        when(performerPerformanceRepository.findAllByPerformanceId(performanceId))
                .thenReturn(performerPerformances);
        when(performerRepository.existsById(any())).thenReturn(true);
        when(performerRepository.save(any())).thenReturn(new Performance());

        UpdatePerformanceResponseDto responseDto =
                performanceAdminService.update(authUser, performanceId, requestDto);

        assertNotNull(responseDto);
        assertEquals(performance.getTitle(), responseDto.getTitle());
    }

    @Test
    void 공연_수정_내용이_비어서_예외_발생() {
        AuthUser authUser = new AuthUser(1L, "a@a.a", "aaaaaa1!", UserRole.ROLE_ADMIN);
        Long performanceId = 1L;
        UpdatePerformanceRequestDto requestDto = new UpdatePerformanceRequestDto();
        User user = new User();
        ReflectionTestUtils.setField(user, "userRole", UserRole.ROLE_ADMIN);
        // When
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(user));
        when(performanceRepository.findById(anyLong())).thenReturn(Optional.of(new Performance()));

        assertThrows(
                NullPointerException.class,
                () -> {
                    performanceAdminService.update(authUser, performanceId, requestDto);
                });
    }

    @Test
    void 공연_정보_수정_중_추가하려는_출연자가_없어서_예외발생() {
        AuthUser authUser = new AuthUser(1L, "a@a.a", "aaaaaa1!", UserRole.ROLE_ADMIN);
        Long performanceId = 1L;
        UpdatePerformanceRequestDto requestDto =
                new UpdatePerformanceRequestDto(
                        "수정테스트",
                        "2024-11-10",
                        "2024-11-15",
                        5000L,
                        Category.CONCERT,
                        "수정 테스트코드",
                        1L,
                        200,
                        new Long[] {1L, 2L});
        User user = new User();
        ReflectionTestUtils.setField(user, "userRole", UserRole.ROLE_ADMIN);
        List<PerformerPerformance> performerPerformances =
                List.of(new PerformerPerformance(), new PerformerPerformance());

        // When
        when(performanceRepository.findById(anyLong())).thenReturn(Optional.of(new Performance()));
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(user));
        when(performerPerformanceRepository.findAllByPerformanceId(performanceId))
                .thenReturn(performerPerformances);
        when(performerRepository.existsById(any())).thenReturn(false);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> {
                            performanceAdminService.update(authUser, performanceId, requestDto);
                        });

        assertEquals("존재하지 않는 출연자입니다.", exception.getMessage());
    }

    @Test
    void 공연_삭제_성공() {
        AuthUser authUser = new AuthUser(1L, "a@a.a", "aaaaaa1!", UserRole.ROLE_ADMIN);
        Long performanceId = 1L;
        User user = new User();
        ReflectionTestUtils.setField(user, "userRole", UserRole.ROLE_ADMIN);
        List<PerformerPerformance> performerPerformances =
                List.of(new PerformerPerformance(), new PerformerPerformance());
        ReflectionTestUtils.setField(performanceAdminService, "bucket", "test-bucket");
        List<Performance> performances = List.of(new Performance(), new Performance());

        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(user));
        when(performerPerformanceRepository.findAllByPerformanceId(performanceId))
                .thenReturn(performerPerformances);
        when(performanceRepository.findAllById(anyLong())).thenReturn(performances);
        when(posterRepository.findByPerformanceId(performanceId))
                .thenReturn(Optional.of(new Poster()));

        performanceAdminService.delete(authUser, performanceId);

        // then
        verify(performanceRepository, times(1)).deleteAll(anyList());
        verify(roundRepository, times(1)).deleteByPerformanceId(anyLong());
        verify(posterRepository, times(1)).delete(any(Poster.class));
    }

    @Test
    void 공연_삭제_시_공연이_없어서_예외발생() {
        AuthUser authUser = new AuthUser(1L, "a@a.a", "aaaaaa1!", UserRole.ROLE_ADMIN);
        Long performanceId = 1L;
        User user = new User();
        ReflectionTestUtils.setField(user, "userRole", UserRole.ROLE_ADMIN);
        List<Performance> performances = List.of();

        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(user));
        when(performanceRepository.findAllById(anyLong())).thenReturn(performances);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> {
                            performanceAdminService.delete(authUser, performanceId);
                        });

        assertEquals("해당 공연이 존재하지 않습니다.", exception.getMessage());
    }

    @Test
    void 배우_등록_성공() {
        Long performanceId = 1L;
        Long performerId = 1L;

        when(performanceRepository.findById(any())).thenReturn(Optional.of(new Performance()));
        when(performerRepository.findById(any())).thenReturn(Optional.of(new Performer()));
        when(performerPerformanceRepository.existsByPerformanceIdAndPerformerId(any(), any()))
                .thenReturn(false);

        performanceAdminService.addPerformer(performanceId, performerId);

        verify(performerPerformanceRepository, times(1)).save(any(PerformerPerformance.class));
    }

    @Test
    void 배우_등록_시_공연을_찾을_수_없어_예외발생() {
        Long performanceId = 1L;
        Long performerId = 1L;

        when(performanceRepository.findById(any())).thenReturn(Optional.empty());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> {
                            performanceAdminService.addPerformer(performanceId, performerId);
                        });

        assertEquals("공연을 찾을 수 없습니다.", exception.getMessage());
    }

    @Test
    void 배우_등록_시_배우를_찾을_수_없어_예외발생() {
        Long performanceId = 1L;
        Long performerId = 1L;

        when(performanceRepository.findById(any())).thenReturn(Optional.of(new Performance()));
        when(performerRepository.findById(any())).thenReturn(Optional.empty());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> {
                            performanceAdminService.addPerformer(performanceId, performerId);
                        });

        assertEquals("배우를 찾을 수 없습니다.", exception.getMessage());
    }

    @Test
    void 배우_등록_시_이미_등록된_공연이라_예외발생() {
        Long performanceId = 1L;
        Long performerId = 1L;

        when(performanceRepository.findById(any())).thenReturn(Optional.of(new Performance()));
        when(performerRepository.findById(any())).thenReturn(Optional.of(new Performer()));
        when(performerPerformanceRepository.existsByPerformanceIdAndPerformerId(any(), any()))
                .thenReturn(true);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> {
                            performanceAdminService.addPerformer(performanceId, performerId);
                        });

        assertEquals("해당 배우는 이미 이 공연에 등록되어 있습니다.", exception.getMessage());
    }

    @Test
    void 배우_삭제_성공() {
        Long performanceId = 1L;
        Long performerId = 1L;

        when(performanceRepository.findById(any())).thenReturn(Optional.of(new Performance()));
        when(performerRepository.findById(any())).thenReturn(Optional.of(new Performer()));
        when(performerPerformanceRepository.findByPerformanceIdAndPerformerId(any(), any()))
                .thenReturn(Optional.of(new PerformerPerformance()));

        performanceAdminService.removePerformer(performanceId, performerId);

        verify(performerPerformanceRepository, times(1)).save(any(PerformerPerformance.class));
    }

    @Test
    void 배우_삭제_시_배우가_공연에_등록되어_있지않아서_예외발생() {
        Long performanceId = 1L;
        Long performerId = 1L;

        when(performanceRepository.findById(any())).thenReturn(Optional.of(new Performance()));
        when(performerRepository.findById(any())).thenReturn(Optional.of(new Performer()));
        when(performerPerformanceRepository.findByPerformanceIdAndPerformerId(any(), any()))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> {
                            performanceAdminService.removePerformer(performanceId, performerId);
                        });

        assertEquals("해당 배우는 이 공연에 등록되어 있지 않습니다.", exception.getMessage());
    }

    @Test
    void 배우_삭제_시_공연을_찾을_수_없어서_예외발생() {
        Long performanceId = 1L;
        Long performerId = 1L;

        when(performanceRepository.findById(any())).thenReturn(Optional.empty());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> {
                            performanceAdminService.removePerformer(performanceId, performerId);
                        });

        assertEquals("공연을 찾을 수 없습니다.", exception.getMessage());
    }

    @Test
    void 배우_삭제_시_배우를_찾지_못해서_예외발생() {
        Long performanceId = 1L;
        Long performerId = 1L;

        when(performanceRepository.findById(any())).thenReturn(Optional.of(new Performance()));
        when(performerRepository.findById(any())).thenReturn(Optional.empty());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> {
                            performanceAdminService.removePerformer(performanceId, performerId);
                        });

        assertEquals("배우를 찾을 수 없습니다.", exception.getMessage());
    }
}
