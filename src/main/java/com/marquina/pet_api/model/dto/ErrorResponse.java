package com.marquina.pet_api.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "Contrato de error uniforme del servicio")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(

        @Schema(description = "Momento en que se produjo el error", example = "2026-07-25T18:50:19.289")
        LocalDateTime timestamp,

        @Schema(description = "Código de estado HTTP", example = "404")
        int status,

        @Schema(description = "Descripción del código de estado", example = "Not Found")
        String error,

        @Schema(description = "Mensaje descriptivo del error", example = "No se encontró la mascota con id 999999999")
        String message,

        @Schema(description = "Ruta solicitada", example = "/api/pet/999999999")
        String path,

        @Schema(
                description = "Identificador de la transacción, presente cuando la operación lo generó",
                example = "60cc5c22-3250-4e07-a519-a6dab99c6713")
        String transactionId,

        @Schema(description = "Detalle por campo cuando la validación del cuerpo falla")
        Map<String, String> fieldErrors
) {
}
