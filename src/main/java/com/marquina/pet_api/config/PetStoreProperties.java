package com.marquina.pet_api.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "petstore.api")
public record PetStoreProperties(

        @NotBlank(message = "petstore.api.base-url es obligatorio")
        String baseUrl,

        @NotBlank(message = "petstore.api.pet-path es obligatorio")
        String petPath,

        @NotBlank(message = "petstore.api.pet-by-id-path es obligatorio")
        String petByIdPath,

        @NotNull(message = "petstore.api.connect-timeout es obligatorio")
        Duration connectTimeout,

        @NotNull(message = "petstore.api.read-timeout es obligatorio")
        Duration readTimeout
) {

    public String petUrl() {
        return baseUrl + petPath;
    }

    public String petByIdUrl() {
        return baseUrl + petByIdPath;
    }
}
