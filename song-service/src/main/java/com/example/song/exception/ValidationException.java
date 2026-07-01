package com.example.song.exception;

import lombok.Getter;

import java.util.Map;

@Getter
public class ValidationException extends RuntimeException {

    private final Map<String, String> details;

    public ValidationException(Map<String, String> details) {
        super("Validation error");
        this.details = details;
    }
}
