package com.marquina.pet_api.model.dto;

public record PetResponse(
        Long id,
        String name,
        String status
) {
}
