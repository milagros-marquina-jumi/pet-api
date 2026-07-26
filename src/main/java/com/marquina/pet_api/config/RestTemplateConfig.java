package com.marquina.pet_api.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Clock;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate petStoreRestTemplate(
            RestTemplateBuilder builder,
            PetStoreProperties properties,
            CorrelationIdInterceptor correlationIdInterceptor) {

        return builder
                .setConnectTimeout(properties.connectTimeout())
                .setReadTimeout(properties.readTimeout())
                .additionalInterceptors(correlationIdInterceptor)
                .build();
    }

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
