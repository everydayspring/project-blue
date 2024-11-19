package com.sparta.projectblue.domain.hall.dto;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.sparta.projectblue.domain.hall.entity.Hall;

import lombok.Getter;

// 캐싱 적용 대상
@Getter
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public class GetHallResponseDto {

    private String name;
    private String address;
    private Integer seats;

    // 캐싱을 위한 기본 생성자
    public GetHallResponseDto() {}

    public GetHallResponseDto(Hall hall) {

        this.name = hall.getName();
        this.address = hall.getAddress();
        this.seats = hall.getSeats();
    }
}
