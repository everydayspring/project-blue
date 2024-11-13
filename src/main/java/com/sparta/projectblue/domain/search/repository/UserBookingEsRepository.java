package com.sparta.projectblue.domain.search.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.sparta.projectblue.domain.search.document.UserBookingDocument;

public interface UserBookingEsRepository
        extends ElasticsearchRepository<UserBookingDocument, Long>, UserBookingEsCustomRepository {}
