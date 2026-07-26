package com.marquina.pet_api.exception;

public class PetNotFoundException extends RuntimeException {

    private final Long petId;

    public PetNotFoundException(Long petId) {
        super("No se encontró la mascota con id " + petId);
        this.petId = petId;
    }

    public Long getPetId() {
        return petId;
    }
}
