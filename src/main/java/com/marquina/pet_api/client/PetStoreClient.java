package com.marquina.pet_api.client;

import com.marquina.pet_api.model.external.PetStorePet;

public interface PetStoreClient {

    PetStorePet findPetById(Long petId);

    PetStorePet createPet(PetStorePet pet);
}
