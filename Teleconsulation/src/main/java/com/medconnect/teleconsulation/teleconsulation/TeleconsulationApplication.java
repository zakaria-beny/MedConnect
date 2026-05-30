package com.medconnect.teleconsulation.teleconsulation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication(scanBasePackages = "com.medconnect.teleconsulation")
@EnableMongoRepositories(basePackages = "com.medconnect.teleconsulation.repository")
@EnableDiscoveryClient
public class TeleconsulationApplication {

    public static void main(String[] args) {
        SpringApplication.run(TeleconsulationApplication.class, args);
    }

}
