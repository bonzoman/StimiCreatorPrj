package com.stimi.creator.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalTime;

@RestController
@RequiredArgsConstructor
public class HealthController {

//    private final com.stimi.creator.biz.spec.MyTelegramBot myTelegramBot;

    @GetMapping("/health")
    public String health() {
        return "OK";
    }

//    @GetMapping("/sendtel")
//    public String sendtel() {
//        myTelegramBot.send("test"+ LocalTime.now());
//        return "OK";
//    }
}
