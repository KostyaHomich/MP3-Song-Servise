package com.example.song.validation;

import com.example.song.dto.SongDto;
import com.example.song.exception.ValidationException;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Component
public class SongValidator {

    private static final Pattern DURATION_PATTERN = Pattern.compile("^([0-5][0-9]):([0-5][0-9])$");
    private static final Pattern YEAR_PATTERN = Pattern.compile("^(19\\d{2}|20\\d{2})$");
    private static final int MIN_TEXT_LENGTH = 1;
    private static final int MAX_TEXT_LENGTH = 100;

    /**
     * Validates the incoming SongDto. Each field produces at most one error message:
     * - if the field is null -> "<Field> is required"
     * - if the field is present but invalid (blank, wrong length, wrong format) -> the
     *   specific format/length message takes precedence over the generic "required" message.
     * Throws ValidationException with a details map if any field is invalid. Does NOT
     * validate the 'id' field (handled separately, since id absence/format has its own
     * dedicated error path per the resource ID conventions).
     */
    public void validate(SongDto dto) {
        Map<String, String> details = new LinkedHashMap<>();

        validateTextField(details, "name", dto.getName(), "Song name");
        validateTextField(details, "artist", dto.getArtist(), "Artist name");
        validateTextField(details, "album", dto.getAlbum(), "Album name");
        validateDuration(details, dto.getDuration());
        validateYear(details, dto.getYear());

        if (!details.isEmpty()) {
            throw new ValidationException(details);
        }
    }

    private void validateTextField(Map<String, String> details, String fieldKey, String value, String displayName) {
        if (value == null) {
            details.put(fieldKey, displayName + " is required");
            return;
        }
        if (value.length() < MIN_TEXT_LENGTH || value.length() > MAX_TEXT_LENGTH) {
            details.put(fieldKey, displayName + " must be between 1 and 100 characters");
        }
    }

    private void validateDuration(Map<String, String> details, String duration) {
        if (duration == null) {
            details.put("duration", "Duration is required");
            return;
        }
        if (!DURATION_PATTERN.matcher(duration).matches()) {
            details.put("duration", "Duration must be in mm:ss format with leading zeros");
        }
    }

    private void validateYear(Map<String, String> details, String year) {
        if (year == null) {
            details.put("year", "Year is required");
            return;
        }
        if (!YEAR_PATTERN.matcher(year).matches()) {
            details.put("year", "Year must be between 1900 and 2099");
        }
    }
}
