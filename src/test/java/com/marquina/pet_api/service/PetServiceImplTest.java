package com.marquina.pet_api.service;

import com.marquina.pet_api.client.PetStoreClient;
import com.marquina.pet_api.config.CorrelationConstants;
import com.marquina.pet_api.exception.ExternalApiException;
import com.marquina.pet_api.exception.PetNotFoundException;
import com.marquina.pet_api.model.dto.CreatePetRequest;
import com.marquina.pet_api.model.dto.CreatePetResponse;
import com.marquina.pet_api.model.dto.PetResponse;
import com.marquina.pet_api.model.external.PetStorePet;
import com.marquina.pet_api.service.impl.PetServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PetServiceImplTest {

    private static final Long PET_ID = 10000023L;
    private static final String PET_NAME = "testingPet1";
    private static final String STATUS_AVAILABLE = "available";

    private static final Instant FIXED_INSTANT = Instant.parse("2024-06-25T19:22:42.181753Z");
    private static final ZoneId FIXED_ZONE = ZoneId.of("UTC");

    @Mock
    private PetStoreClient petStoreClient;

    private PetServiceImpl petService() {
        return new PetServiceImpl(petStoreClient, Clock.fixed(FIXED_INSTANT, FIXED_ZONE));
    }

    @Test
    void mapsOnlyContractFieldsFromExternalPet() {
        when(petStoreClient.findPetById(PET_ID)).thenReturn(fullExternalPet());

        PetResponse response = petService().getPetById(PET_ID);

        assertThat(response.id()).isEqualTo(PET_ID);
        assertThat(response.name()).isEqualTo(PET_NAME);
        assertThat(response.status()).isEqualTo(STATUS_AVAILABLE);
    }

    @Test
    void throwsNotFoundWhenClientReturnsNull() {
        when(petStoreClient.findPetById(PET_ID)).thenReturn(null);

        assertThatThrownBy(() -> petService().getPetById(PET_ID))
                .isInstanceOf(PetNotFoundException.class);
    }

    @Test
    void propagatesNotFoundRaisedByClient() {
        when(petStoreClient.findPetById(PET_ID)).thenThrow(new PetNotFoundException(PET_ID));

        assertThatThrownBy(() -> petService().getPetById(PET_ID))
                .isInstanceOf(PetNotFoundException.class);
    }

    @Test
    void keepsNullFieldsInsteadOfFailingWhenExternalDataIsIncomplete() {
        when(petStoreClient.findPetById(PET_ID))
                .thenReturn(new PetStorePet(PET_ID, null, null, null, null, null));

        PetResponse response = petService().getPetById(PET_ID);

        assertThat(response.id()).isEqualTo(PET_ID);
        assertThat(response.name()).isNull();
        assertThat(response.status()).isNull();
    }

    @Test
    void generatesTransactionIdAndTimestampOnCreate() {
        when(petStoreClient.createPet(any(PetStorePet.class))).thenReturn(createdExternalPet());

        CreatePetResponse response = petService().createPet(validRequest());

        assertThat(response.transactionId()).isNotBlank();
        assertThat(UUID.fromString(response.transactionId()).version()).isEqualTo(4);
        assertThat(response.dateCreated())
                .isEqualTo(LocalDateTime.ofInstant(FIXED_INSTANT, FIXED_ZONE));
        assertThat(response.status()).isTrue();
        assertThat(response.name()).isEqualTo(PET_NAME);
    }

    @Test
    void generatesDistinctTransactionIdPerInvocation() {
        when(petStoreClient.createPet(any(PetStorePet.class))).thenReturn(createdExternalPet());
        PetServiceImpl service = petService();

        String first = service.createPet(validRequest()).transactionId();
        String second = service.createPet(validRequest()).transactionId();

        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void clearsTransactionIdFromContextAfterCreate() {
        when(petStoreClient.createPet(any(PetStorePet.class))).thenReturn(createdExternalPet());

        petService().createPet(validRequest());

        assertThat(MDC.get(CorrelationConstants.TRANSACTION_ID_MDC_KEY)).isNull();
    }

    @Test
    void clearsTransactionIdEvenWhenUpstreamFails() {
        when(petStoreClient.createPet(any(PetStorePet.class)))
                .thenThrow(new ExternalApiException("fallo", null));

        assertThatThrownBy(() -> petService().createPet(validRequest()))
                .isInstanceOf(ExternalApiException.class);

        assertThat(MDC.get(CorrelationConstants.TRANSACTION_ID_MDC_KEY)).isNull();
    }

    @Test
    void failsWhenUpstreamReturnsNoBodyOnCreate() {
        when(petStoreClient.createPet(any(PetStorePet.class))).thenReturn(null);

        assertThatThrownBy(() -> petService().createPet(validRequest()))
                .isInstanceOf(ExternalApiException.class);
    }

    private CreatePetRequest validRequest() {
        return new CreatePetRequest(PET_ID, STATUS_AVAILABLE, PET_NAME);
    }

    private PetStorePet createdExternalPet() {
        return new PetStorePet(PET_ID, null, PET_NAME, List.of(), List.of(), STATUS_AVAILABLE);
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
