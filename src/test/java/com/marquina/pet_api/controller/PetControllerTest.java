package com.marquina.pet_api.controller;

import com.marquina.pet_api.exception.GlobalExceptionHandler;
import com.marquina.pet_api.exception.PetNotFoundException;
import com.marquina.pet_api.model.dto.CreatePetResponse;
import com.marquina.pet_api.model.dto.PetResponse;
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
import java.time.LocalDateTime;
import java.time.Month;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PetController.class)
@Import({GlobalExceptionHandler.class, PetControllerTest.ClockTestConfig.class})
class PetControllerTest {

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
    void getPetOk() throws Exception {
        when(petService.getPetById(PET_ID))
                .thenReturn(new PetResponse(PET_ID, "testingPet1", "available"));

        mockMvc.perform(get("/api/pet/{petId}", PET_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(PET_ID))
                .andExpect(jsonPath("$.name").value("testingPet1"))
                .andExpect(jsonPath("$.status").value("available"))
                .andExpect(jsonPath("$.category").doesNotExist())
                .andExpect(jsonPath("$.photoUrls").doesNotExist())
                .andExpect(jsonPath("$.tags").doesNotExist());
    }

    @Test
    void getPetNotFound() throws Exception {
        when(petService.getPetById(anyLong())).thenThrow(new PetNotFoundException(999999999L));

        mockMvc.perform(get("/api/pet/{petId}", 999999999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void getPetConIdNoNumericoDevuelve400() throws Exception {
        mockMvc.perform(get("/api/pet/{petId}", "abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postPetOk() throws Exception {
        when(petService.createPet(any())).thenReturn(new CreatePetResponse(
                "60cc5c22-3250-4e07-a519-a6dab99c6713",
                LocalDateTime.of(2024, Month.JUNE, 25, 19, 22, 42, 181753000),
                true,
                "testingPet1"));

        mockMvc.perform(post("/api/pet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":10000023,"status":"available","name":"testingPet1"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("60cc5c22-3250-4e07-a519-a6dab99c6713"))
                .andExpect(jsonPath("$.dateCreated").value("2024-06-25T19:22:42.181753"))
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.name").value("testingPet1"));
    }

    @Test
    void postPetConStatusInvalidoDevuelve400() throws Exception {
        mockMvc.perform(post("/api/pet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":10000023,"status":"invalido","name":"testingPet1"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.status").exists());
    }

    @Test
    void postPetSinCamposDevuelve400() throws Exception {
        mockMvc.perform(post("/api/pet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.id").exists());
    }
}
