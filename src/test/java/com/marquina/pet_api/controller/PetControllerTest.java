package com.marquina.pet_api.controller;

import com.marquina.pet_api.exception.GlobalExceptionHandler;
import com.marquina.pet_api.exception.PetNotFoundException;
import com.marquina.pet_api.model.dto.PetResponse;
import com.marquina.pet_api.service.PetService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PetController.class)
@Import({GlobalExceptionHandler.class, PetControllerTest.ClockTestConfig.class})
class PetControllerTest {

    private static final String PET_BY_ID_URL = "/api/pet/{petId}";
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
    void returnsPetWithContractFieldsOnly() throws Exception {
        when(petService.getPetById(PET_ID))
                .thenReturn(new PetResponse(PET_ID, "testingPet1", "available"));

        mockMvc.perform(get(PET_BY_ID_URL, PET_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(PET_ID))
                .andExpect(jsonPath("$.name").value("testingPet1"))
                .andExpect(jsonPath("$.status").value("available"))
                .andExpect(jsonPath("$.category").doesNotExist())
                .andExpect(jsonPath("$.photoUrls").doesNotExist())
                .andExpect(jsonPath("$.tags").doesNotExist());
    }

    @Test
    void returnsNotFoundWhenPetDoesNotExist() throws Exception {
        when(petService.getPetById(anyLong())).thenThrow(new PetNotFoundException(999999999L));

        mockMvc.perform(get(PET_BY_ID_URL, 999999999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").value("/api/pet/999999999"));
    }

    @Test
    void returnsBadRequestWhenPetIdIsNotNumeric() throws Exception {
        mockMvc.perform(get(PET_BY_ID_URL, "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
