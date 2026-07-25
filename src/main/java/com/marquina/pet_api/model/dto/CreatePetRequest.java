package com.marquina.pet_api.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreatePetRequest(

        @NotNull(message = "id es obligatorio")
        @Positive(message = "id debe ser un numero positivo")
        Long id,

        @NotBlank(message = "status es obligatorio")
        @Pattern(regexp = "available|pending|sold", message = "status debe ser available, pending o sold")
        String status,

        @NotBlank(message = "name es obligatorio")
        @Size(max = 100, message = "name no puede exceder 100 caracteres")
        String name
) {
}
