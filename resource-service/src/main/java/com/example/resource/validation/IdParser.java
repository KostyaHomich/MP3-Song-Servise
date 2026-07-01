package com.example.resource.validation;

import com.example.resource.exception.BadRequestException;

import java.util.ArrayList;
import java.util.List;

public final class IdParser {

    private static final int MAX_CSV_LENGTH = 200;

    private IdParser() {
    }

    /**
     * Parses a single path-variable ID string. Throws BadRequestException with the exact
     * specification message if the value is not a positive integer.
     */
    public static long parsePathId(String rawId) {
        if (rawId == null || !rawId.matches("\\d+")) {
            throw new BadRequestException("Invalid value '" + rawId + "' for ID. Must be a positive integer");
        }
        long value;
        try {
            value = Long.parseLong(rawId);
        } catch (NumberFormatException ex) {
            throw new BadRequestException("Invalid value '" + rawId + "' for ID. Must be a positive integer");
        }
        if (value <= 0) {
            throw new BadRequestException("Invalid value '" + rawId + "' for ID. Must be a positive integer");
        }
        return value;
    }

    /**
     * Parses a comma-separated list of IDs used by DELETE endpoints. Validates max length
     * first, then validates each token is a positive integer, throwing the exact
     * specification messages on failure.
     */
    public static List<Long> parseCsvIds(String csv) {
        if (csv == null || csv.isBlank()) {
            throw new BadRequestException("Invalid ID format: ''. Only positive integers are allowed");
        }

        if (csv.length() > MAX_CSV_LENGTH) {
            throw new BadRequestException(
                    "CSV string is too long: received " + csv.length() + " characters, maximum allowed is " + MAX_CSV_LENGTH);
        }

        List<Long> ids = new ArrayList<>();
        for (String token : csv.split(",", -1)) {
            String trimmed = token.trim();
            if (!trimmed.matches("\\d+")) {
                throw new BadRequestException("Invalid ID format: '" + trimmed + "'. Only positive integers are allowed");
            }
            try {
                long value = Long.parseLong(trimmed);
                if (value <= 0) {
                    throw new BadRequestException("Invalid ID format: '" + trimmed + "'. Only positive integers are allowed");
                }
                ids.add(value);
            } catch (NumberFormatException ex) {
                throw new BadRequestException("Invalid ID format: '" + trimmed + "'. Only positive integers are allowed");
            }
        }

        return ids;
    }
}
