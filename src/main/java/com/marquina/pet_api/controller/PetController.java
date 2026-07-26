package com.marquina.pet_api.controller;

import com.marquina.pet_api.model.dto.CreatePetRequest;
import com.marquina.pet_api.model.dto.CreatePetResponse;
import com.marquina.pet_api.model.dto.ErrorResponse;
import com.marquina.pet_api.model.dto.PetResponse;
import com.marquina.pet_api.service.PetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pet")
@Tag(name = "Pet", description = "Consulta y registro de mascotas")
public class PetController {

    private final PetService petService;

    public PetController(PetService petService) {
        this.petService = petService;
    }

    @Operation(
            summary = "Consultar una mascota por identificador",
            description = """
                    Obtiene la mascota desde el sistema externo y devuelve únicamente los tres \
                    campos del contrato. Los campos propios de Petstore (category, photoUrls, tags) \
                    no se propagan.""")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Mascota encontrada",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PetResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "id": 10000023,
                                      "name": "testingPet1",
                                      "status": "available"
                                    }"""))),
            @ApiResponse(
                    responseCode = "400",
                    description = "El identificador no es un número válido",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2026-07-25T18:50:19.341",
                                      "status": 400,
                                      "error": "Bad Request",
                                      "message": "El parámetro 'petId' debe ser un número válido",
                                      "path": "/api/pet/abc"
                                    }"""))),
            @ApiResponse(
                    responseCode = "404",
                    description = "La mascota no existe",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2026-07-25T18:50:19.289",
                                      "status": 404,
                                      "error": "Not Found",
                                      "message": "No se encontró la mascota con id 999999999",
                                      "path": "/api/pet/999999999"
                                    }"""))),
            @ApiResponse(
                    responseCode = "502",
                    description = "El sistema externo respondió con error",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2026-07-25T18:50:19.700",
                                      "status": 502,
                                      "error": "Bad Gateway",
                                      "message": "PetStore respondió con un error",
                                      "path": "/api/pet"
                                    }"""))),
            @ApiResponse(
                    responseCode = "504",
                    description = "El sistema externo no respondió dentro del tiempo configurado",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2026-07-25T18:50:24.100",
                                      "status": 504,
                                      "error": "Gateway Timeout",
                                      "message": "PetStore no respondió a tiempo",
                                      "path": "/api/pet"
                                    }""")))
    })
    @GetMapping("/{petId}")
    public ResponseEntity<PetResponse> getPet(
            @Parameter(description = "Identificador de la mascota", example = "10000023", required = true)
            @PathVariable Long petId) {

        return ResponseEntity.ok(petService.getPetById(petId));
    }

    @Operation(
            summary = "Registrar una mascota",
            description = """
                    Registra la mascota en el sistema externo y devuelve el resultado de la operación.

                    Los campos transactionId (UUIDv4) y dateCreated se generan en la capa de servicio; \
                    no provienen del sistema externo. El campo status del response es booleano e indica \
                    el éxito de la operación, a diferencia del status del request, que es el estado de \
                    la mascota.

                    La operación no es idempotente a nivel de transacción: cada invocación genera un \
                    transactionId distinto, aunque el sistema externo sobrescriba la misma mascota.""")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Mascota registrada",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CreatePetResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "transactionId": "60cc5c22-3250-4e07-a519-a6dab99c6713",
                                      "dateCreated": "2026-07-25T18:50:19.513",
                                      "status": true,
                                      "name": "testingPet1"
                                    }"""))),
            @ApiResponse(
                    responseCode = "400",
                    description = "El cuerpo es inválido o no es un JSON válido",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2026-07-25T18:50:19.566",
                                      "status": 400,
                                      "error": "Bad Request",
                                      "message": "La solicitud contiene campos inválidos",
                                      "path": "/api/pet",
                                      "fieldErrors": {
                                        "status": "El status debe ser available, pending o sold"
                                      }
                                    }"""))),
            @ApiResponse(
                    responseCode = "502",
                    description = "El sistema externo respondió con error",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2026-07-25T18:50:19.700",
                                      "status": 502,
                                      "error": "Bad Gateway",
                                      "message": "PetStore respondió con un error",
                                      "path": "/api/pet"
                                    }"""))),
            @ApiResponse(
                    responseCode = "504",
                    description = "El sistema externo no respondió dentro del tiempo configurado",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2026-07-25T18:50:24.100",
                                      "status": 504,
                                      "error": "Gateway Timeout",
                                      "message": "PetStore no respondió a tiempo",
                                      "path": "/api/pet"
                                    }""")))
    })
    @PostMapping
    public ResponseEntity<CreatePetResponse> createPet(@Valid @RequestBody CreatePetRequest request) {
        return ResponseEntity.ok(petService.createPet(request));
    }
}
