package com.marquina.pet_api.service.impl;

import com.marquina.pet_api.client.PetStoreClient;
import com.marquina.pet_api.config.CorrelationConstants;
import com.marquina.pet_api.exception.ExternalApiException;
import com.marquina.pet_api.exception.PetNotFoundException;
import com.marquina.pet_api.model.dto.CreatePetRequest;
import com.marquina.pet_api.model.dto.CreatePetResponse;
import com.marquina.pet_api.model.dto.PetResponse;
import com.marquina.pet_api.model.external.PetStorePet;
import com.marquina.pet_api.service.PetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PetServiceImpl implements PetService {

    private static final Logger log = LoggerFactory.getLogger(PetServiceImpl.class);

    private final PetStoreClient petStoreClient;
    private final Clock clock;

    public PetServiceImpl(PetStoreClient petStoreClient, Clock clock) {
        this.petStoreClient = petStoreClient;
        this.clock = clock;
    }

    @Override
    public PetResponse getPetById(Long petId) {
        PetStorePet pet = Optional.ofNullable(petStoreClient.findPetById(petId))
                .orElseThrow(() -> new PetNotFoundException(petId));

        log.info("Mascota obtenida | id={} name={} status={}", pet.id(), pet.name(), pet.status());

        return new PetResponse(pet.id(), pet.name(), pet.status());
    }

    @Override
    public CreatePetResponse createPet(CreatePetRequest request) {
        // Se genera al inicio para que aparezca en los logs de la operación.
        String transactionId = UUID.randomUUID().toString();
        MDC.put(CorrelationConstants.TRANSACTION_ID_MDC_KEY, transactionId);

        try {
            log.info("Iniciando registro de mascota | id={}", request.id());

            PetStorePet created = Optional
                    .ofNullable(petStoreClient.createPet(toPetStorePet(request)))
                    .orElseThrow(() -> new ExternalApiException(
                            "PetStore no devolvió la mascota registrada", null));

            log.info("Mascota registrada | id={} name={} status={}",
                    created.id(), created.name(), created.status());

            return new CreatePetResponse(
                    transactionId,
                    LocalDateTime.now(clock),
                    true,
                    created.name());
        } finally {
            // Tomcat reutiliza hilos, sin esto se filtra al siguiente request.
            MDC.remove(CorrelationConstants.TRANSACTION_ID_MDC_KEY);
        }
    }

    private PetStorePet toPetStorePet(CreatePetRequest request) {
        return new PetStorePet(request.id(), null, request.name(), null, null, request.status());
    }
}
