package com.mediconnect.dmp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// @SpringBootApplication = tells Spring: "This is the main class, start everything from here"
@SpringBootApplication
public class DmpServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DmpServiceApplication.class, args);
    }
}