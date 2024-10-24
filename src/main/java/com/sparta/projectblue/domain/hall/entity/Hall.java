package com.sparta.projectblue.domain.hall.entity;

import com.sparta.projectblue.domain.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "halls")
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

    public void update(String name, String address, int seats) {
        this.name = name;
        this.address = address;
        this.seats = seats;
    }
}
