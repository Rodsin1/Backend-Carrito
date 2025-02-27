package com.mitiendita.carritoservice.exception;

public class OrdenNotFoundException extends RuntimeException {

    public OrdenNotFoundException(String message) {
        super(message);
    }
}