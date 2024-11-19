package com.sparta.projectblue.domain.search.repository;

import java.util.List;

import com.sparta.projectblue.domain.search.document.UserBookingDocument;
import com.sparta.projectblue.domain.search.dto.UserBookingDto;

public interface UserBookingEsCustomRepository {

    List<UserBookingDocument> searchByCriteria(UserBookingDto request);
}
