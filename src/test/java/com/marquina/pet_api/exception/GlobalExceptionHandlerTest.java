package com.marquina.pet_api.exception;

import com.marquina.pet_api.controller.PetController;
import com.marquina.pet_api.service.PetService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PetController.class)
@Import({GlobalExceptionHandler.class, GlobalExceptionHandlerTest.ClockTestConfig.class})
class GlobalExceptionHandlerTest {

    private static final String PET_BY_ID_URL = "/api/pet/{petId}";
    private static final String MESSAGE_JSON_PATH = "$.message";
    private static final Long PET_ID = 10000023L;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PetService petService;

    @TestConfiguration
    static class ClockTestConfig {
        @Bean
        Clock clock() {
            return Clock.systemDefaultZone();
        }
    }

    @Test
    void mapsUpstreamTimeoutToGatewayTimeout() throws Exception {
        when(petService.getPetById(anyLong()))
                .thenThrow(new ExternalApiTimeoutException("PetStore no respondió a tiempo", null));

        mockMvc.perform(get(PET_BY_ID_URL, PET_ID))
                .andExpect(status().isGatewayTimeout())
                .andExpect(jsonPath("$.status").value(504))
                .andExpect(jsonPath("$.error").value("Gateway Timeout"));
    }

    @Test
    void mapsUpstreamFailureToBadGateway() throws Exception {
        when(petService.getPetById(anyLong()))
                .thenThrow(new ExternalApiException("PetStore respondió con un error", null));

        mockMvc.perform(get(PET_BY_ID_URL, PET_ID))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value(502));
    }

    @Test
    void hidesInternalDetailsOnUnexpectedFailure() throws Exception {
        when(petService.getPetById(anyLong()))
                .thenThrow(new IllegalStateException("jdbc://internal-host:5432 credenciales expuestas"));

        mockMvc.perform(get(PET_BY_ID_URL, PET_ID))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath(MESSAGE_JSON_PATH).value("Ocurrió un error inesperado al procesar la solicitud"))
                .andExpect(jsonPath(MESSAGE_JSON_PATH).value(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("internal-host"))));
    }

    @Test
    void returnsErrorContractFieldsOnEveryFailure() throws Exception {
        when(petService.getPetById(anyLong())).thenThrow(new PetNotFoundException(PET_ID));

        mockMvc.perform(get(PET_BY_ID_URL, PET_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath(MESSAGE_JSON_PATH).exists())
                .andExpect(jsonPath("$.path").value("/api/pet/" + PET_ID));
    }

    @Test
    void rejectsMalformedJsonBody() throws Exception {
        mockMvc.perform(post("/api/pet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ esto no es json valido "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath(MESSAGE_JSON_PATH).value("El cuerpo de la solicitud no es un JSON válido"));
    }
}
