package com.marquina.pet_api.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PetStorePropertiesTest {

    @Autowired
    private PetStoreProperties properties;

    @Autowired
    private RestTemplate petStoreRestTemplate;

    @Test
    void bindsPetStoreConfigurationFromEnvironment() {
        assertThat(properties.baseUrl()).isEqualTo("http://localhost:9999/v2");
        assertThat(properties.connectTimeout()).isEqualTo(Duration.ofSeconds(1));
        assertThat(properties.readTimeout()).isEqualTo(Duration.ofSeconds(2));
    }

    @Test
    void registersCorrelationInterceptorOnRestTemplate() {
        assertThat(petStoreRestTemplate.getInterceptors())
                .hasAtLeastOneElementOfType(CorrelationIdInterceptor.class);
    }
}
