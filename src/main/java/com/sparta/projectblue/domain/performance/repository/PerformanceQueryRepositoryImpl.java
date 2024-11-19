package com.sparta.projectblue.domain.performance.repository;

import static com.sparta.projectblue.domain.hall.entity.QHall.hall;
import static com.sparta.projectblue.domain.performance.entity.QPerformance.performance;
import static com.sparta.projectblue.domain.performer.entity.QPerformer.performer;
import static com.sparta.projectblue.domain.performerperformance.entity.QPerformerPerformance.performerPerformance;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.projectblue.domain.performance.dto.GetPerformancePerformersResponseDto;
import com.sparta.projectblue.domain.performance.dto.GetPerformancesResponseDto;
import com.sparta.projectblue.domain.search.document.SearchDocument;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PerformanceQueryRepositoryImpl implements PerformanceQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    private static final String START = "startDate";
    private static final String END = "endDate";

    @Override
    public Page<GetPerformancesResponseDto> findAllPerformance(Pageable pageable) {

        List<GetPerformancesResponseDto> query =
                jpaQueryFactory
                        .select(
                                Projections.fields(
                                        GetPerformancesResponseDto.class,
                                        performance.title.as("title"),
                                        hall.name.as("hallNm"),
                                        performance.startDate.as(START),
                                        performance.endDate.as(END)))
                        .from(performance)
                        .leftJoin(hall)
                        .on(performance.hallId.eq(hall.id))
                        .where(performanceBetweenIn(LocalDateTime.now()))
                        .orderBy(performance.startDate.asc())
                        .offset(pageable.getOffset())
                        .limit(pageable.getPageSize())
                        .fetch();

        Long total =
                jpaQueryFactory
                        .select(performance.count())
                        .from(performance)
                        .leftJoin(hall)
                        .on(performance.hallId.eq(hall.id))
                        .where(performanceBetweenIn(LocalDateTime.now()))
                        .fetchOne();

        if (total == null) total = 0L;

        return new PageImpl<>(query, pageable, total);
    }

    @Override
    public Page<GetPerformancesResponseDto> findByCondition(
            Pageable pageable,
            String performanceNm,
            LocalDateTime performanceDay,
            String performerNm) {

        List<GetPerformancesResponseDto> query =
                jpaQueryFactory
                        .select(
                                Projections.fields(
                                        GetPerformancesResponseDto.class,
                                        performance.title.as("title"),
                                        hall.name.as("hallNm"),
                                        performance.startDate.as(START),
                                        performance.endDate.as(END)))
                        .from(performance)
                        .leftJoin(hall)
                        .on(performance.hallId.eq(hall.id))
                        .leftJoin(performerPerformance)
                        .on(performance.id.eq(performerPerformance.performanceId))
                        .leftJoin(performer)
                        .on(performerPerformance.performerId.eq(performer.id))
                        .where(
                                containPerformanceNm(performanceNm),
                                performanceBetweenIn(performanceDay),
                                containPerformerNm(performerNm))
                        .orderBy(performance.startDate.asc())
                        .offset(pageable.getOffset())
                        .limit(pageable.getPageSize())
                        .fetch();

        Long total =
                jpaQueryFactory
                        .select(performance.count())
                        .from(performance)
                        .leftJoin(hall)
                        .on(performance.hallId.eq(hall.id))
                        .leftJoin(performerPerformance)
                        .on(performance.id.eq(performerPerformance.performanceId))
                        .leftJoin(performer)
                        .on(performerPerformance.performerId.eq(performer.id))
                        .where(
                                containPerformanceNm(performanceNm),
                                performanceBetweenIn(performanceDay),
                                containPerformerNm(performerNm))
                        .fetchOne();

        if (total == null) total = 0L;

        return new PageImpl<>(query, pageable, total);
    }

    private BooleanExpression containPerformanceNm(String performanceNm) {

        return (performanceNm != null && !performanceNm.isEmpty())
                ? performance.title.containsIgnoreCase(performanceNm)
                : null;
    }

    private BooleanExpression containPerformerNm(String performerNm) {

        return (performerNm != null && !performerNm.isEmpty())
                ? performer.name.containsIgnoreCase(performerNm)
                : null;
    }

    private BooleanExpression performanceBetweenIn(LocalDateTime performanceDay) {

        BooleanExpression afterStart = performance.startDate.loe(performanceDay);

        BooleanExpression beforeEnd = performance.endDate.goe(performanceDay);

        return afterStart.and(beforeEnd);
    }

    @Override
    public List<GetPerformancePerformersResponseDto.PerformerInfo> findPerformersByPerformanceId(
            Long performanceId) {

        return jpaQueryFactory
                .select(
                        Projections.constructor(
                                GetPerformancePerformersResponseDto.PerformerInfo.class,
                                performer.name,
                                performer.nation,
                                performer.birth))
                .from(performer)
                .join(performerPerformance)
                .on(performer.id.eq(performerPerformance.performerId))
                .where(performerPerformance.performanceId.eq(performanceId))
                .fetch();
    }

    @Override
    public List<SearchDocument> findForESDocument() {
        // Performance 기본 정보 조회
        List<SearchDocument> performances =
                jpaQueryFactory
                        .select(
                                Projections.constructor(
                                        SearchDocument.class,
                                        performance.id.as("performanceId"),
                                        hall.id.as("hallId"),
                                        performance.title.as("performanceTitle"),
                                        performance.startDate.as(START),
                                        performance.endDate.as(END),
                                        performance.price.as("price"),
                                        performance.category.stringValue().as("category"),
                                        performance.description.as("description"),
                                        performance.duration.as("duration"),
                                        Projections.constructor(
                                                SearchDocument.Hall.class,
                                                hall.name.as("hallName"),
                                                hall.address.as("hallAddress"),
                                                hall.seats.as("hallSeats"))))
                        .from(performance)
                        .leftJoin(hall)
                        .on(performance.hallId.eq(hall.id))
                        .fetch();

        // 각 Performance에 대한 Performer 정보 조회 후 매핑
        performances.forEach(
                doc -> {
                    List<SearchDocument.PerformerInfo> performers =
                            jpaQueryFactory
                                    .select(
                                            Projections.constructor(
                                                    SearchDocument.PerformerInfo.class,
                                                    performer.id.as("performerId"),
                                                    performer.name.as("performerName"),
                                                    performer.birth.as("birth"),
                                                    performer.nation.as("nation")))
                                    .from(performer)
                                    .join(performerPerformance)
                                    .on(performer.id.eq(performerPerformance.performerId))
                                    .where(
                                            performerPerformance.performanceId.eq(
                                                    doc.getPerformanceId()))
                                    .fetch();

                    doc.setPerformers(performers);
                });

        return performances;
    }
}
