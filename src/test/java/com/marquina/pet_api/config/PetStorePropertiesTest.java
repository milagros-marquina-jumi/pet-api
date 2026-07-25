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
    void bindsPetStoreConfigurationFromProperties() {
        assertThat(properties.baseUrl()).isEqualTo("https://petstore.swagger.io/v2");
        assertThat(properties.connectTimeout()).isEqualTo(Duration.ofSeconds(3));
        assertThat(properties.readTimeout()).isEqualTo(Duration.ofSeconds(5));
    }

    @Test
    void registersCorrelationInterceptorOnRestTemplate() {
        assertThat(petStoreRestTemplate.getInterceptors())
                .hasAtLeastOneElementOfType(CorrelationIdInterceptor.class);
    }
}
