package com.marquina.pet_api.service;

import com.marquina.pet_api.client.PetStoreClient;
import com.marquina.pet_api.exception.PetNotFoundException;
import com.marquina.pet_api.model.dto.PetResponse;
import com.marquina.pet_api.model.external.PetStorePet;
import com.marquina.pet_api.service.impl.PetServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PetServiceImplTest {

    private static final Long PET_ID = 10000023L;
    private static final String PET_NAME = "testingPet1";
    private static final String STATUS_AVAILABLE = "available";

    @Mock
    private PetStoreClient petStoreClient;

    @InjectMocks
    private PetServiceImpl petService;

    @Test
    void mapsOnlyContractFieldsFromExternalPet() {
        when(petStoreClient.findPetById(PET_ID)).thenReturn(fullExternalPet());

        PetResponse response = petService.getPetById(PET_ID);

        assertThat(response.id()).isEqualTo(PET_ID);
        assertThat(response.name()).isEqualTo(PET_NAME);
        assertThat(response.status()).isEqualTo(STATUS_AVAILABLE);
    }

    @Test
    void throwsNotFoundWhenClientReturnsNull() {
        when(petStoreClient.findPetById(PET_ID)).thenReturn(null);

        assertThatThrownBy(() -> petService.getPetById(PET_ID))
                .isInstanceOf(PetNotFoundException.class);
    }

    @Test
    void propagatesNotFoundRaisedByClient() {
        when(petStoreClient.findPetById(PET_ID)).thenThrow(new PetNotFoundException(PET_ID));

        assertThatThrownBy(() -> petService.getPetById(PET_ID))
                .isInstanceOf(PetNotFoundException.class);
    }

    @Test
    void keepsNullFieldsInsteadOfFailingWhenExternalDataIsIncomplete() {
        when(petStoreClient.findPetById(PET_ID))
                .thenReturn(new PetStorePet(PET_ID, null, null, null, null, null));

        PetResponse response = petService.getPetById(PET_ID);

        assertThat(response.id()).isEqualTo(PET_ID);
        assertThat(response.name()).isNull();
        assertThat(response.status()).isNull();
    }

    private PetStorePet fullExternalPet() {
        return new PetStorePet(
                PET_ID,
                new PetStorePet.Category(1L, "dogs"),
                PET_NAME,
                List.of("http://photo.test/1.jpg"),
                List.of(new PetStorePet.Tag(1L, "tag")),
                STATUS_AVAILABLE);
    }
}
