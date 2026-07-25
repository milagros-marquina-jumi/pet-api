package com.marquina.pet_api.service;

import com.marquina.pet_api.model.dto.CreatePetRequest;
import com.marquina.pet_api.model.dto.CreatePetResponse;
import com.marquina.pet_api.model.dto.PetResponse;

public interface PetService {

    PetResponse getPetById(Long petId);

    CreatePetResponse createPet(CreatePetRequest request);
}
