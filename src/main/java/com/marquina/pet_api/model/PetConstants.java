package com.marquina.pet_api.model;

public final class PetConstants {

    public static final String STATUS_AVAILABLE = "available";
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_SOLD = "sold";

    public static final String STATUS_PATTERN = STATUS_AVAILABLE + "|" + STATUS_PENDING + "|" + STATUS_SOLD;

    public static final int NAME_MAX_LENGTH = 100;

    private PetConstants() {
    }
}
