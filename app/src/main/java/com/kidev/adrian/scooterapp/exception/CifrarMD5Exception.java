package com.kidev.adrian.scooterapp.exception;

public class CifrarMD5Exception extends RuntimeException {
    public CifrarMD5Exception() {
    }

    public CifrarMD5Exception(String message) {
        super(message);
    }

    public CifrarMD5Exception(String message, Throwable cause) {
        super(message, cause);
    }

    public CifrarMD5Exception(Throwable cause) {
        super(cause);
    }
}
