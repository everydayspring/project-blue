package com.sparta.projectblue.domain.search.document;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Document(indexName = "user_booking_index_v4")
public class UserBookingDocument {
    @Id private Long reservationId;

    @Field(type = FieldType.Text)
    private String userName;

    @Field(type = FieldType.Long)
    private Long userId;

    @Field(type = FieldType.Text)
    private String performanceTitle;

    @Field(
            type = FieldType.Date,
            format = {},
            pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime bookingDate; // 예매한 날짜

    @Field(type = FieldType.Long)
    private Long paymentAmount; // 예매한 티켓 금액

    @Field(type = FieldType.Text)
    private String reservationStatus; // 예매 상태

    @Field(type = FieldType.Long)
    private Long paymentId; // 결제id (추가구현 가능하려나)

    @Field(
            type = FieldType.Date,
            format = {},
            pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime paymentDate; // 결제 된 날짜(추가구현 가능하려나2)

    @Builder
    public UserBookingDocument(
            Long reservationId,
            String userName,
            Long userId,
            String performanceTitle,
            LocalDateTime bookingDate,
            Long paymentAmount,
            String reservationStatus,
            Long paymentId,
            LocalDateTime paymentDate) {
        this.reservationId = reservationId;
        this.userName = userName;
        this.userId = userId;
        this.performanceTitle = performanceTitle;
        this.bookingDate = bookingDate;
        this.paymentAmount = paymentAmount;
        this.reservationStatus = reservationStatus;
        this.paymentId = paymentId;
        this.paymentDate = paymentDate;
    }
}
