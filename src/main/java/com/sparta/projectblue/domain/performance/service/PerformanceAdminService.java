package com.sparta.projectblue.domain.performance.service;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
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
import com.sparta.projectblue.domain.performer.repository.PerformerRepository;
import com.sparta.projectblue.domain.performerperformance.entity.PerformerPerformance;
import com.sparta.projectblue.domain.performerperformance.repository.PerformerPerformanceRepository;
import com.sparta.projectblue.domain.poster.entity.Poster;
import com.sparta.projectblue.domain.poster.repository.PosterRepository;
import com.sparta.projectblue.domain.round.repository.RoundRepository;
import com.sparta.projectblue.domain.user.entity.User;
import com.sparta.projectblue.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PerformanceAdminService {

    private final PerformanceRepository performanceRepository;

    private final HallRepository hallRepository;
    private final PerformerRepository performerRepository;
    private final PerformerPerformanceRepository performerPerformanceRepository;
    private final PosterRepository posterRepository;
    private final UserRepository userRepository;
    private final RoundRepository roundRepository;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final AmazonS3 amazonS3;

    @Transactional
    public CreatePerformanceResponseDto create(
            AuthUser authUser, CreatePerformanceRequestDto request, MultipartFile posterFile) {

        hasRole(authUser);

        if (!hallRepository.existsById(request.getHallId())) {
            throw new IllegalArgumentException("존재하지 않는 공연장입니다.");
        }

        LocalDateTime startDate = LocalDate.parse(request.getStartDate()).atStartOfDay();

        if (LocalDateTime.now().isAfter(startDate)) {
            throw new IllegalArgumentException("시작일이 현재보다 이전일 수 없습니다.");
        }

        LocalDateTime endDate = LocalDate.parse(request.getEndDate()).atTime(LocalTime.MAX);

        if (LocalDateTime.now().isAfter(endDate)) {
            throw new IllegalArgumentException("종료일이 현재보다 이전일 수 없습니다.");
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("종료일이 시작일보다 빠를 수 없습니다.");
        }

        if (request.getDuration() == 0) {
            throw new IllegalArgumentException("공연시간을 입력해주세요.");
        }

        Category category = Category.of(request.getCategory());

        Performance performance =
                new Performance(
                        request.getHallId(),
                        request.getTitle(),
                        startDate,
                        endDate,
                        request.getPrice(),
                        category,
                        request.getDescription(),
                        request.getDuration());

        // 공연 등록
        Performance savedPerformance = performanceRepository.save(performance);

        // 출연자 등록
        for (Long performerId : request.getPerformers()) {
            if (!performerRepository.existsById(performerId)) {
                throw new IllegalArgumentException("존재하지 않는 출연자입니다.");
            }

            PerformerPerformance per =
                    new PerformerPerformance(performerId, savedPerformance.getId());
            performerPerformanceRepository.save(per);
        }

        // aws 추가
        String posterName = "images/" + createFileName(posterFile.getOriginalFilename());

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(posterFile.getSize());
        objectMetadata.setContentType(posterFile.getContentType());

        String posterUrl;

        try (InputStream inputStream = posterFile.getInputStream()) {
            log.info("Uploading file: " + posterName + " to bucket: " + bucket);
            amazonS3.putObject(
                    new PutObjectRequest(bucket, posterName, inputStream, objectMetadata));
            log.info("File uploaded successfully.");
            posterUrl = amazonS3.getUrl(bucket, posterName).toString();
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다.");
        }

        // 포스터 등록
        Poster poster =
                new Poster(savedPerformance.getId(), posterName, posterUrl, posterFile.getSize());
        posterRepository.save(poster);

        return new CreatePerformanceResponseDto(
                savedPerformance.getId(),
                savedPerformance.getTitle(),
                savedPerformance.getStartDate(),
                savedPerformance.getEndDate(),
                savedPerformance.getPrice(),
                savedPerformance.getCategory(),
                savedPerformance.getDescription(),
                savedPerformance.getDuration());
    }

    @Transactional
    public UpdatePerformanceResponseDto update(
            AuthUser authUser, Long performanceId, UpdatePerformanceRequestDto requestDto) {

        hasRole(authUser);

        Performance performance =
                performanceRepository
                        .findById(performanceId)
                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공연입니다."));

        // 공연 정보 수정
        performance.update(requestDto);

        // 출연자 리스트 전체 삭제 후 다시 추가
        deletePerformerPerformance(performanceId);

        for (Long performerId : requestDto.getPerformers()) {
            if (!performerRepository.existsById(performerId)) {
                throw new IllegalArgumentException("존재하지 않는 출연자입니다.");
            }

            PerformerPerformance per = new PerformerPerformance(performerId, performanceId);
            performerPerformanceRepository.save(per);
        }

        return new UpdatePerformanceResponseDto(performance.getId(), performance.getTitle());
    }

    @Transactional
    public void delete(AuthUser authUser, Long performanceId) {

        hasRole(authUser);

        List<Performance> performances = performanceRepository.findAllById(performanceId);

        if (performances.isEmpty()) {
            throw new IllegalArgumentException("해당 공연이 존재하지 않습니다.");
        }

        performanceRepository.deleteAll(performances);

        // 관련 회차 삭제
        roundRepository.deleteByPerformanceId(performanceId);

        // 공연, 출연자 테이블 연관데이터 삭제
        deletePerformerPerformance(performanceId);

        // 포스터 테이블 삭제
        Poster poster =
                posterRepository
                        .findByPerformanceId(performanceId)
                        .orElseThrow(() -> new IllegalArgumentException("공연에 대한 포스터가 없습니다."));

        log.info("Deleting poster file: " + poster.getName() + " from bucket: " + bucket);
        amazonS3.deleteObject(new DeleteObjectRequest(bucket, poster.getName()));
        log.info("Poster file deleted successfully from S3.");

        posterRepository.delete(poster);
    }

    public void hasRole(AuthUser authUser) {
        // DB에서의 권한도 검증
        User user =
                userRepository
                        .findById(authUser.getId())
                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        if (user.getUserRole() != UserRole.ROLE_ADMIN) {
            throw new IllegalArgumentException("관리자만 접근할 수 있습니다.");
        }
    }

    private void deletePerformerPerformance(Long performanceId) {

        List<PerformerPerformance> performerPerformances =
                performerPerformanceRepository.findAllByPerformanceId(performanceId);

        if (performerPerformances.isEmpty()) {
            return;
        }

        performerPerformanceRepository.deleteAll(performerPerformances);
    }

    public String createFileName(String fileName) {

        // aws 업로드를 위한 unique name 생성
        return UUID.randomUUID().toString().concat(getFileExtension(fileName));
    }

    private String getFileExtension(String fileName) {

        try {
            return fileName.substring(fileName.lastIndexOf("."));
        } catch (StringIndexOutOfBoundsException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "잘못된 형식의 파일(" + fileName + ") 입니다.");
        }
    }

    @Transactional
    public void addPerformer(Long performanceId, Long performerId) {

        if (!performanceRepository.existsById(performanceId)) {
            throw new IllegalArgumentException("공연을 찾을 수 없습니다.");
        }

        if (!performerRepository.existsById(performerId)) {
            throw new IllegalArgumentException("배우를 찾을 수 없습니다.");
        }

        if (performerPerformanceRepository.existsByPerformanceIdAndPerformerId(
                performanceId, performerId)) {
            throw new IllegalArgumentException("해당 배우는 이미 이 공연에 등록되어 있습니다.");
        }

        PerformerPerformance performerPerformance =
                new PerformerPerformance(performerId, performanceId);

        performerPerformanceRepository.save(performerPerformance);
    }

    @Transactional
    public void removePerformer(Long performanceId, Long performerId) {

        if (!performanceRepository.existsById(performanceId)) {
            throw new IllegalArgumentException("공연을 찾을 수 없습니다.");
        }

        if (!performerRepository.existsById(performerId)) {
            throw new IllegalArgumentException("배우를 찾을 수 없습니다.");
        }

        PerformerPerformance performerPerformance =
                performerPerformanceRepository
                        .findByPerformanceIdAndPerformerId(performanceId, performerId)
                        .orElseThrow(
                                () -> new IllegalArgumentException("해당 배우는 이 공연에 등록되어 있지 않습니다."));

        performerPerformanceRepository.delete(performerPerformance);
    }
}
