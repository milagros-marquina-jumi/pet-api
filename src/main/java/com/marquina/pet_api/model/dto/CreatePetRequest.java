package com.marquina.pet_api.model.dto;

import com.marquina.pet_api.model.PetConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreatePetRequest(

        @NotNull(message = "El id es obligatorio")
        @Positive(message = "El id debe ser un número positivo")
        Long id,

        @NotBlank(message = "El status es obligatorio")
        @Pattern(
                regexp = PetConstants.STATUS_PATTERN,
                message = "El status debe ser available, pending o sold")
        String status,

        @NotBlank(message = "El name es obligatorio")
        @Size(max = PetConstants.NAME_MAX_LENGTH, message = "El name no puede exceder 100 caracteres")
        String name
) {
}
