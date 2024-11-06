package com.sparta.projectblue.domain.performer.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sparta.projectblue.domain.performer.entity.Performer;

import lombok.Getter;

@Getter
public class UpdatePerformerResponseDto {

    private final Long id;
    private final String name;
    private final String birth;
    private final String nation;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime modifiedAt;

    public UpdatePerformerResponseDto(Performer performer) {

        this.id = performer.getId();
        this.name = performer.getName();
        this.birth = performer.getBirth();
        this.nation = performer.getNation();
        this.createdAt = performer.getCreatedAt();
        this.modifiedAt = performer.getModifiedAt();
    }
}
