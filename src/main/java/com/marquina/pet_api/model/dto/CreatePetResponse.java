package com.marquina.pet_api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Resultado del registro de la mascota")
public record CreatePetResponse(

        @Schema(
                description = "Identificador único de la transacción, generado en la capa de servicio",
                example = "60cc5c22-3250-4e07-a519-a6dab99c6713")
        String transactionId,

        @Schema(
                description = "Fecha y hora de la operación según el sistema",
                example = "2026-07-25T18:50:19.513")
        LocalDateTime dateCreated,

        @Schema(
                description = "Indica si la operación fue exitosa. No es el estado de la mascota",
                example = "true")
        boolean status,

        @Schema(description = "Nombre de la mascota registrada", example = "testingPet1")
        String name
) {
}
