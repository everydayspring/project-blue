package com.sparta.projectblue.domain.hall.controller;

import com.sparta.projectblue.domain.hall.service.HallService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/halls")
public class HallController {
    private final HallService hallService;


}





