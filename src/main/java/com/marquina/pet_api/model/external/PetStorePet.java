package com.marquina.pet_api.model.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

// Modelo de Petstore. No se expone al cliente.
@JsonIgnoreProperties(ignoreUnknown = true)
public record PetStorePet(
        Long id,
        Category category,
        String name,
        List<String> photoUrls,
        List<Tag> tags,
        String status
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Category(Long id, String name) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Tag(Long id, String name) {
    }
}
