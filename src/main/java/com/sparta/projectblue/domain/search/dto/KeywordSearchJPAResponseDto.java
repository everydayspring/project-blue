package com.sparta.projectblue.domain.search.dto;

import org.springframework.data.domain.Page;

import com.sparta.projectblue.domain.hall.entity.Hall;
import com.sparta.projectblue.domain.performance.entity.Performance;
import com.sparta.projectblue.domain.performer.entity.Performer;

import lombok.Getter;

@Getter
public class KeywordSearchJPAResponseDto {
    private final Page<Performer> performers;
    private final Page<Performance> searchDocuments;
    private final Page<Hall> halls;

    public KeywordSearchJPAResponseDto(
            Page<Performer> performers, Page<Performance> searchDocuments, Page<Hall> halls) {
        this.performers = performers;
        this.searchDocuments = searchDocuments;
        this.halls = halls;
    }
}
