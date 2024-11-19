package com.sparta.projectblue.domain.performer.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.sparta.projectblue.domain.performer.entity.Performer;

import lombok.Getter;

@Getter
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public class GetPerformersResponseDto {

    private List<PerformerInfo> performers;

    // 기본 생성자 추가
    public GetPerformersResponseDto() {}

    public GetPerformersResponseDto(List<PerformerInfo> performers) {
        this.performers = performers;
    }

    @Getter
    public static class PerformerInfo {

        private String name;
        private String birth;

        // 기본 생성자 추가
        public PerformerInfo() {}

        public PerformerInfo(Performer performer) {
            this.name = performer.getName();
            this.birth = performer.getBirth();
        }
    }
}
