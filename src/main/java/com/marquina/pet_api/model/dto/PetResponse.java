package com.marquina.pet_api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos de la mascota expuestos por este servicio")
public record PetResponse(

        @Schema(description = "Identificador de la mascota", example = "10000023")
        Long id,

        @Schema(description = "Nombre de la mascota", example = "testingPet1")
        String name,

        @Schema(description = "Estado de la mascota", example = "available")
        String status
) {
}
