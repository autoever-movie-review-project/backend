package com.movie;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class MoviePlayApplication {
    public static void main(String[] args) {
        SpringApplication.run(MoviePlayApplication.class, args);

    }
}