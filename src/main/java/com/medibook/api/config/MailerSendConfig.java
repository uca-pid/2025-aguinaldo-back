package com.medibook.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class MailerSendConfig {

    @Value("${mailersend.api.token}")
    private String apiToken;

    @Bean
    public WebClient mailerSendWebClient() {
        return WebClient.builder()
                .baseUrl("https://api.mailersend.com/v1")
                .defaultHeader("Authorization", "Bearer " + apiToken)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}