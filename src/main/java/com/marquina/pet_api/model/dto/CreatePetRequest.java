package com.marquina.pet_api.model.dto;

import com.marquina.pet_api.model.PetConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos necesarios para registrar una mascota")
public record CreatePetRequest(

        @Schema(description = "Identificador de la mascota", example = "10000023", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "El id es obligatorio")
        @Positive(message = "El id debe ser un número positivo")
        Long id,

        @Schema(
                description = "Estado de la mascota",
                example = "available",
                allowableValues = {"available", "pending", "sold"},
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "El status es obligatorio")
        @Pattern(
                regexp = PetConstants.STATUS_PATTERN,
                message = "El status debe ser available, pending o sold")
        String status,

        @Schema(description = "Nombre de la mascota", example = "testingPet1", maxLength = 100, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "El name es obligatorio")
        @Size(max = PetConstants.NAME_MAX_LENGTH, message = "El name no puede exceder 100 caracteres")
        String name
) {
}
