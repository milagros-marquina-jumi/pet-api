package com.marquina.pet_api.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marquina.pet_api.model.dto.CreatePetResponse;
import com.marquina.pet_api.model.dto.PetResponse;
import com.marquina.pet_api.model.external.PetStorePet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class SerializationSanityTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void deserializesPetStoreResponseIntoRecord() throws Exception {
        String petstorePayload = """
                {"id":1,"category":{"id":1,"name":"string"},"name":"doggie",
                 "photoUrls":["string"],"tags":[{"id":1,"name":"string"}],"status":"available"}
                """;

        PetStorePet pet = objectMapper.readValue(petstorePayload, PetStorePet.class);

        assertThat(pet.id()).isEqualTo(1L);
        assertThat(pet.name()).isEqualTo("doggie");
        assertThat(pet.status()).isEqualTo("available");
        assertThat(pet.category().name()).isEqualTo("string");
    }

    @Test
    void ignoresUnknownFieldsFromExternalApi() throws Exception {
        String payloadWithNewField = """
                {"id":1,"name":"doggie","status":"available","campoNuevoDelTercero":"algo"}
                """;

        PetStorePet pet = objectMapper.readValue(payloadWithNewField, PetStorePet.class);

        assertThat(pet.id()).isEqualTo(1L);
    }

    @Test
    void petResponseExposesOnlyContractFields() throws Exception {
        String json = objectMapper.writeValueAsString(new PetResponse(1L, "doggie", "available"));

        assertThat(json).contains("\"id\"", "\"name\"", "\"status\"");
        assertThat(json).doesNotContain("category", "photoUrls", "tags");
    }

    @Test
    void serializesLocalDateTimeAsIsoWithoutExtraConfiguration() throws Exception {
        LocalDateTime fixed = LocalDateTime.of(2024, 6, 25, 19, 22, 42, 181753000);

        String json = objectMapper.writeValueAsString(
                new CreatePetResponse("60cc5c22-3250-4e07-a519-a6dab99c6713", fixed, true, "testingPet1"));

        assertThat(json).contains("\"dateCreated\":\"2024-06-25T19:22:42.181753\"");
        assertThat(json).contains("\"status\":true");
    }
}
