package com.sparta.projectblue.domain.performer.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.sparta.projectblue.domain.performer.entity.Performer;

import lombok.Getter;

@Getter
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public class GetPerformerResponseDto {

    private String name;
    private String nation;
    private String birth;

    // 기본 생성자 추가
    public GetPerformerResponseDto() {}

    @JsonCreator
    public GetPerformerResponseDto(
            @JsonProperty("name") String name,
            @JsonProperty("nation") String nation,
            @JsonProperty("birth") String birth) {
        this.name = name;
        this.nation = nation;
        this.birth = birth;
    }

    public GetPerformerResponseDto(Performer performer) {

        this.name = performer.getName();
        this.nation = performer.getNation();
        this.birth = performer.getBirth();
    }
}
