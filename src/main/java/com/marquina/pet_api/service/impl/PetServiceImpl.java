package com.marquina.pet_api.service.impl;

import com.marquina.pet_api.client.PetStoreClient;
import com.marquina.pet_api.exception.PetNotFoundException;
import com.marquina.pet_api.model.dto.PetResponse;
import com.marquina.pet_api.model.external.PetStorePet;
import com.marquina.pet_api.service.PetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PetServiceImpl implements PetService {

    private static final Logger log = LoggerFactory.getLogger(PetServiceImpl.class);

    private final PetStoreClient petStoreClient;

    public PetServiceImpl(PetStoreClient petStoreClient) {
        this.petStoreClient = petStoreClient;
    }

    @Override
    public PetResponse getPetById(Long petId) {
        PetStorePet pet = Optional.ofNullable(petStoreClient.findPetById(petId))
                .orElseThrow(() -> new PetNotFoundException(petId));

        log.info("Mascota obtenida | id={} name={} status={}", pet.id(), pet.name(), pet.status());

        return new PetResponse(pet.id(), pet.name(), pet.status());
    }
}
