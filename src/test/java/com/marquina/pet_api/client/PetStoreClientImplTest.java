package com.marquina.pet_api.client;

import com.marquina.pet_api.client.impl.PetStoreClientImpl;
import com.marquina.pet_api.config.PetStoreProperties;
import com.marquina.pet_api.exception.ExternalApiException;
import com.marquina.pet_api.exception.PetNotFoundException;
import com.marquina.pet_api.model.external.PetStorePet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class PetStoreClientImplTest {

    private static final String BASE_URL = "http://petstore.test/v2";
    private static final String PET_NAME = "testingPet1";
    private static final String STATUS_AVAILABLE = "available";
    private static final Long PET_ID = 10000023L;

    private MockRestServiceServer server;
    private PetStoreClientImpl client;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        server = MockRestServiceServer.createServer(restTemplate);
        PetStoreProperties properties = new PetStoreProperties(
                BASE_URL, "/pet", "/pet/{petId}", Duration.ofSeconds(3), Duration.ofSeconds(5));
        client = new PetStoreClientImpl(restTemplate, properties);
    }

    @Test
    void mapsPetStoreResponseWhenPetExists() {
        server.expect(requestTo(BASE_URL + "/pet/" + PET_ID))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"id":10000023,"category":{"id":1,"name":"dogs"},"name":"testingPet1",
                         "photoUrls":["url"],"tags":[{"id":1,"name":"tag"}],"status":"available"}
                        """, MediaType.APPLICATION_JSON));

        PetStorePet pet = client.findPetById(PET_ID);

        assertThat(pet.id()).isEqualTo(PET_ID);
        assertThat(pet.name()).isEqualTo(PET_NAME);
        assertThat(pet.status()).isEqualTo(STATUS_AVAILABLE);
        server.verify();
    }

    @Test
    void translatesNotFoundIntoDomainException() {
        server.expect(requestTo(BASE_URL + "/pet/999999999"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"code\":1,\"type\":\"error\",\"message\":\"Pet not found\"}"));

        assertThatThrownBy(() -> client.findPetById(999999999L))
                .isInstanceOf(PetNotFoundException.class)
                .hasMessageContaining("999999999");
        server.verify();
    }

    @Test
    void translatesServerErrorIntoExternalApiException() {
        server.expect(requestTo(BASE_URL + "/pet/1"))
                .andRespond(withServerError());

        assertThatThrownBy(() -> client.findPetById(1L))
                .isInstanceOf(ExternalApiException.class);
        server.verify();
    }

    @Test
    void sendsPetAsJsonBodyOnCreate() {
        server.expect(requestTo(BASE_URL + "/pet"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.id").value(PET_ID))
                .andExpect(jsonPath("$.name").value(PET_NAME))
                .andExpect(jsonPath("$.status").value(STATUS_AVAILABLE))
                .andRespond(withSuccess("""
                        {"id":10000023,"name":"testingPet1","photoUrls":[],"tags":[],"status":"available"}
                        """, MediaType.APPLICATION_JSON));

        PetStorePet created = client.createPet(
                new PetStorePet(PET_ID, null, PET_NAME, null, null, STATUS_AVAILABLE));

        assertThat(created.name()).isEqualTo(PET_NAME);
        server.verify();
    }
}
