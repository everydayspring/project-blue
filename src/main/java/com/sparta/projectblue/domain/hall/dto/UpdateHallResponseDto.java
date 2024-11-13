package com.sparta.projectblue.domain.hall.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sparta.projectblue.domain.hall.entity.Hall;

import lombok.Getter;

@Getter
public class UpdateHallResponseDto {

    private final Long id;
    private final String name;
    private final String address;
    private final Integer seats;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime modifiedAt;

    public UpdateHallResponseDto(Hall hall) {

        this.id = hall.getId();
        this.name = hall.getName();
        this.address = hall.getAddress();
        this.seats = hall.getSeats();
        this.createdAt = hall.getCreatedAt();
        this.modifiedAt = hall.getModifiedAt();
    }
}
