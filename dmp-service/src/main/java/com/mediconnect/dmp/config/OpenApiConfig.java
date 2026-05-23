package com.mediconnect.dmp.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MedConnect DMP Service API")
                        .version("1.0.0")
                        .description("Digital Medical Profile (DMP) - Patient health records management API")
                        .contact(new Contact()
                                .name("MedConnect Team")
                                .email("support@mediconnect.com")
                                .url("https://mediconnect.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}
