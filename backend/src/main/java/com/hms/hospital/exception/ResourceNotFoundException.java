package com.hms.hospital.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String entity) {
        super(entity + " not found");
    }
}
