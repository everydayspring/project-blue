package com.sparta.projectblue.domain.hall.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import com.sparta.projectblue.domain.common.entity.BaseEntity;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "halls", indexes = @Index(name = "idx_hall_name", columnList = "name"))
public class Hall extends BaseEntity {

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(nullable = false)
    private int seats;

    public Hall(String name, String address, int seats) {

        this.name = name;
        this.address = address;
        this.seats = seats;
    }

    // 공연장 정보 수정 메서드
    public void update(String name, String address, Integer seats) {

        if (name != null && !name.isEmpty()) {
            this.name = name;
        }

        if (address != null && !address.isEmpty()) {
            this.address = address;
        }

        if (seats != null && seats > 0) {
            this.seats = seats;
        }
    }
}
