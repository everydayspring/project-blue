package com.sparta.projectblue.domain.performance.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.projectblue.domain.performance.dto.PerformanceResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import static com.sparta.projectblue.domain.hall.entity.QHall.hall;
import static com.sparta.projectblue.domain.performance.entity.QPerformance.performance;
import static com.sparta.projectblue.domain.performer.entity.QPerformer.performer;
import static com.sparta.projectblue.domain.performerperformance.entity.QPerformerPerformance.performerPerformance;

@Repository
@RequiredArgsConstructor
public class PerformanceRepositoryImpl implements PerformanceRepositoryCustom{

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<PerformanceResponseDto> findByCondition(Pageable pageable, String performanceNm, LocalDateTime performanceDay, String performerNm) {

        List<PerformanceResponseDto> query = jpaQueryFactory
                .select(
                        Projections.fields(PerformanceResponseDto.class,
                                performance.title.as("title"),
                                hall.name.as("hallNm"),
                                performance.startDate.as("startDate"),
                                performance.endDate.as("endDate")
                        )
                )
                .from(performance)
                .leftJoin(hall).on(performance.hallId.eq(hall.id))
                .leftJoin(performerPerformance).on(performance.id.eq(performerPerformance.performanceId))
                .leftJoin(performer).on(performerPerformance.performerId.eq(performer.id))
                .where(
                        containPerformanceNm(performanceNm),
                        performanceBetweenIn(performanceDay),
                        containPerformerNm(performerNm)
                )
                .orderBy(performance.startDate.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = jpaQueryFactory
                .select(performance.count())
                .from(performance)
                .leftJoin(hall).on(performance.hallId.eq(hall.id))
                .leftJoin(performerPerformance).on(performance.id.eq(performerPerformance.performanceId))
                .leftJoin(performer).on(performerPerformance.performerId.eq(performer.id))
                .where(
                        containPerformanceNm(performanceNm),
                        performanceBetweenIn(performanceDay),
                        containPerformerNm(performerNm)
                )
                .fetchOne();

        if(total == null) total = 0L;

        return new PageImpl<>(query, pageable, total);
    }

    private BooleanExpression containPerformanceNm(String performanceNm) {
        return (performanceNm != null && !performanceNm.isEmpty())
                ? performance.title.containsIgnoreCase(performanceNm) : null;
    }

    private BooleanExpression containPerformerNm(String performerNm) {
        return (performerNm != null && !performerNm.isEmpty())
                ? performer.name.containsIgnoreCase(performerNm) : null;
    }

    private BooleanExpression performanceBetweenIn(LocalDateTime performanceDay) {
        BooleanExpression afterStart = performance.startDate.loe(performanceDay);
        BooleanExpression beforeEnd = performance.endDate.goe(performanceDay);

        return afterStart.and(beforeEnd);
    }
}