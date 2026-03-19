package com.stimi.creator;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
public class StimiCreatorApplication {
    //..
    public static void main(String[] args) {
        SpringApplication.run(StimiCreatorApplication.class, args);
    }
}
