package com.medconnect.userservice.config;

import org.springframework.context.annotation.Configuration;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;

@Configuration
public class EnvConfig {

    public EnvConfig() {
        loadEnvFile();
    }

    private void loadEnvFile() {
        String envPath = ".env";
        try (BufferedReader reader = new BufferedReader(new FileReader(envPath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Skip comments and empty lines
                if (line.trim().isEmpty() || line.trim().startsWith("#")) {
                    continue;
                }
                // Parse KEY=VALUE
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    String key = parts[0].trim();
                    String value = parts[1].trim();
                    // Set as system property if not already set
                    if (System.getenv(key) == null) {
                        System.setProperty(key, value);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Warning: .env file not found. Using default values.");
        }
    }
}
