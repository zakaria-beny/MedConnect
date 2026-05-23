package com.medconnect.teleconsulation.teleconsulation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.medconnect.teleconsulation")
public class TeleconsulationApplication {

    public static void main(String[] args) {
        SpringApplication.run(TeleconsulationApplication.class, args);
    }

}
