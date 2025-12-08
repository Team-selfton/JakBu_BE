package com.jakbu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class JakBuApplication {

    public static void main(String[] args) {
        SpringApplication.run(JakBuApplication.class, args);
    }

}

