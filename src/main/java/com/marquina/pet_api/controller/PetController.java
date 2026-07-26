package com.marquina.pet_api.controller;

import com.marquina.pet_api.model.dto.CreatePetRequest;
import com.marquina.pet_api.model.dto.CreatePetResponse;
import com.marquina.pet_api.model.dto.PetResponse;
import com.marquina.pet_api.service.PetService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pet")
public class PetController {

    private final PetService petService;

    public PetController(PetService petService) {
        this.petService = petService;
    }

    @GetMapping("/{petId}")
    public ResponseEntity<PetResponse> getPet(@PathVariable Long petId) {
        return ResponseEntity.ok(petService.getPetById(petId));
    }

    @PostMapping
    public ResponseEntity<CreatePetResponse> createPet(@Valid @RequestBody CreatePetRequest request) {
        return ResponseEntity.ok(petService.createPet(request));
    }
}
