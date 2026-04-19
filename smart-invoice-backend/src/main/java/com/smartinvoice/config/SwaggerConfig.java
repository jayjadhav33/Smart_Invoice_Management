package com.smartinvoice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Smart Invoice & Billing Management API")
                        .version("1.0.0")
                        .description("REST APIs for Smart Invoice System - " +
                                "Manage Clients, Products and Invoices")
                        .contact(new Contact()
                                .name("Smart Invoice Team")
                                .email("admin@smartinvoice.com")));
    }
}