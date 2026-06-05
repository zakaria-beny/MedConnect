package com.medconnect.teleconsulation; // 👈 only change this line

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication // 👈 you can also remove scanBasePackages, no longer needed
public class TeleconsulationApplication {

    public static void main(String[] args) {
        SpringApplication.run(TeleconsulationApplication.class, args);
    }
}