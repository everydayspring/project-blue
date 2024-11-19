package com.sparta.projectblue.domain.performance.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.sparta.projectblue.domain.performer.entity.Performer;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public class GetPerformancePerformersResponseDto {

    private List<PerformerInfo> performers;

    // 캐싱을 위한 기본 생성자
    public GetPerformancePerformersResponseDto() {}

    public GetPerformancePerformersResponseDto(List<PerformerInfo> performers) {
        this.performers = performers;
    }

    @Getter
    // @NoArgsConstructor
    @AllArgsConstructor
    public static class PerformerInfo {

        private String name;
        private String nation;
        private String birth;

        public PerformerInfo() {}

        public PerformerInfo(Performer performer) {

            this.name = performer.getName();
            this.nation = performer.getNation();
            this.birth = performer.getBirth();
        }
    }
}
