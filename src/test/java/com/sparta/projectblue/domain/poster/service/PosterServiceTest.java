package com.sparta.projectblue.domain.poster.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.sparta.projectblue.domain.common.dto.AuthUser;
import com.sparta.projectblue.domain.common.enums.UserRole;
import com.sparta.projectblue.domain.performance.service.PerformanceAdminService;
import com.sparta.projectblue.domain.poster.dto.UpdatePosterResponseDto;
import com.sparta.projectblue.domain.poster.entity.Poster;
import com.sparta.projectblue.domain.poster.repository.PosterRepository;

@ExtendWith(SpringExtension.class)
class PosterServiceTest {
    @Mock private PosterRepository posterRepository;

    @Mock private PerformanceAdminService performanceAdminService;

    @Mock private AmazonS3 amazonS3;

    @InjectMocks private PosterService posterService;

    @Mock private MultipartFile multipartFile;

    @Captor private ArgumentCaptor<DeleteObjectRequest> deleteCaptor;

    @Captor private ArgumentCaptor<PutObjectRequest> putCaptor;

    private AuthUser authUser;
    private Poster poster;
    private final Long posterId = 1L;
    private final String bucketName = "test-bucket";
    private final String posterName = "images/newPoster.jpg";
    private final String posterUrl = "https://test-bucket.s3.amazonaws.com/images/newPoster.jpg";

    @BeforeEach
    void setUp() {
        authUser = new AuthUser(1L, "user@email.com", "taeju", UserRole.ROLE_ADMIN);
        poster =
                new Poster(
                        1L,
                        "images/newPoster.jpg",
                        "https://test-bucket.s3.amazonaws.com/oldPoster.jpg",
                        1024L);
        ReflectionTestUtils.setField(posterService, "bucket", bucketName);
    }

    @Test
    void updatePosterTest() throws IOException {
        // given
        when(performanceAdminService.createFileName(anyString())).thenReturn("newPoster.jpg");
        when(multipartFile.getOriginalFilename()).thenReturn("newPoster.jpg");
        when(multipartFile.getContentType()).thenReturn("image/jpeg");
        when(multipartFile.getSize()).thenReturn(1024L);
        when(multipartFile.getInputStream())
                .thenReturn(new ByteArrayInputStream("mock data".getBytes()));

        when(posterRepository.findById(posterId)).thenReturn(Optional.of(poster));
        doNothing().when(performanceAdminService).hasRole(authUser);
        when(amazonS3.getUrl(bucketName, posterName)).thenReturn(new java.net.URL(posterUrl));

        // when
        UpdatePosterResponseDto response = posterService.update(authUser, posterId, multipartFile);

        // then
        assertNotNull(response);
        assertEquals(poster.getPerformanceId(), response.getPerformanceId());
        assertEquals(posterName, response.getName());
        assertEquals(posterUrl, response.getImageUrl());

        verify(amazonS3, times(1)).deleteObject(deleteCaptor.capture());
        verify(amazonS3, times(1)).putObject(putCaptor.capture());
        verify(posterRepository, times(1)).findById(posterId);

        DeleteObjectRequest deleteRequest = deleteCaptor.getValue();
        assertEquals(bucketName, deleteRequest.getBucketName());
        assertEquals(poster.getName(), deleteRequest.getKey());

        PutObjectRequest putRequest = putCaptor.getValue();
        assertEquals(bucketName, putRequest.getBucketName());
        assertEquals(poster.getName(), putRequest.getKey());
    }

    @Test
    void updatePosterWhenPosterNotFound() {
        // given
        when(posterRepository.findById(posterId)).thenReturn(Optional.empty());

        Exception exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> posterService.update(authUser, posterId, multipartFile));

        assertEquals("존재하지 않는 포스터입니다.", exception.getMessage());
        verify(posterRepository, times(1)).findById(posterId);
    }

    @Test
    void updatePosterWhenS3UploadFails() throws IOException {
        when(performanceAdminService.createFileName(anyString())).thenReturn("newPoster.jpg");
        when(multipartFile.getOriginalFilename()).thenReturn("newPoster.jpg");
        when(multipartFile.getContentType()).thenReturn("image/jpeg");
        when(multipartFile.getSize()).thenReturn(1024L);
        when(multipartFile.getInputStream()).thenThrow(new IOException("S3 Upload Error"));

        when(posterRepository.findById(posterId)).thenReturn(Optional.of(poster));
        doNothing().when(performanceAdminService).hasRole(authUser);

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> posterService.update(authUser, posterId, multipartFile));

        assertEquals("새 이미지 업로드에 실패했습니다.", exception.getReason());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        verify(posterRepository, times(1)).findById(posterId);
    }

    @Test
    void updatePosterWhenUserNotAuthorized() {
        doThrow(new IllegalStateException("권한이 없습니다."))
                .when(performanceAdminService)
                .hasRole(authUser);

        Exception exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> posterService.update(authUser, posterId, multipartFile));

        assertEquals("권한이 없습니다.", exception.getMessage());
        verify(performanceAdminService, times(1)).hasRole(authUser);
        verifyNoInteractions(amazonS3);
    }
}
