package com.example.resource.service;

import com.example.resource.dto.SongMetadataDto;
import com.example.resource.exception.InvalidMp3Exception;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.metadata.XMPDM;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;

@Slf4j
@Service
public class Mp3MetadataExtractor {

    private static final String AUDIO_MPEG_MIME = "audio/mpeg";

    public SongMetadataDto extract(byte[] data) {
        if (data == null || data.length == 0) {
            throw new InvalidMp3Exception("MP3 data is empty");
        }

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data)) {
            Metadata metadata = new Metadata();
            BodyContentHandler handler = new BodyContentHandler(-1);
            AutoDetectParser parser = new AutoDetectParser();
            ParseContext context = new ParseContext();

            parser.parse(inputStream, handler, metadata, context);

            String mimeType = metadata.get(Metadata.CONTENT_TYPE);
            if (mimeType == null || !mimeType.startsWith("audio/mpeg")) {
                throw new InvalidMp3Exception(
                        "Uploaded file is not a valid MP3 (detected MIME: " + mimeType + ")");
            }

            String name = getFirstNonNull(
                    metadata.get(TikaCoreProperties.TITLE),
                    metadata.get("dc:title"),
                    metadata.get("title"),
                    "Unknown"
            );

            String artist = getFirstNonNull(
                    metadata.get(XMPDM.ARTIST),
                    metadata.get("xmpDM:artist"),
                    metadata.get("creator"),
                    metadata.get("dc:creator"),
                    "Unknown"
            );

            String album = getFirstNonNull(
                    metadata.get(XMPDM.ALBUM),
                    metadata.get("xmpDM:album"),
                    metadata.get("album"),
                    "Unknown"
            );

            String durationRaw = getFirstNonNull(
                    metadata.get(XMPDM.DURATION),
                    metadata.get("xmpDM:duration"),
                    metadata.get("duration")
            );

            String duration = parseDuration(durationRaw);

            String year = getFirstNonNull(
                    metadata.get(XMPDM.RELEASE_DATE),
                    metadata.get("xmpDM:releaseDate"),
                    metadata.get("year"),
                    metadata.get("date"),
                    metadata.get("dc:date")
            );

            // Normalize year to 4 digits if it contains a full date
            if (year != null && year.length() > 4) {
                year = year.substring(0, 4);
            }

            log.info("Extracted metadata: name={}, artist={}, album={}, duration={}, year={}",
                    name, artist, album, duration, year);

            return SongMetadataDto.builder()
                    .name(name)
                    .artist(artist)
                    .album(album)
                    .duration(duration)
                    .year(year)
                    .build();

        } catch (InvalidMp3Exception ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InvalidMp3Exception("Failed to parse MP3 file: " + ex.getMessage(), ex);
        }
    }

    /**
     * Converts a duration value (in seconds, possibly as a decimal string) to mm:ss format.
     * Tika may return duration as milliseconds or seconds depending on the tag.
     */
    private String parseDuration(String durationRaw) {
        if (durationRaw == null || durationRaw.isBlank()) {
            return "00:00";
        }

        try {
            double durationSeconds = Double.parseDouble(durationRaw.trim());

            // Tika's XMPDM.DURATION is in milliseconds for some formats; treat > 10000 as ms
            if (durationSeconds > 10000) {
                durationSeconds = durationSeconds / 1000.0;
            }

            long totalSeconds = Math.round(durationSeconds);
            long minutes = totalSeconds / 60;
            long seconds = totalSeconds % 60;
            return String.format("%02d:%02d", minutes, seconds);
        } catch (NumberFormatException ex) {
            log.warn("Could not parse duration value: {}", durationRaw);
            return "00:00";
        }
    }

    private String getFirstNonNull(String... values) {
        for (String val : values) {
            if (val != null && !val.isBlank()) {
                return val.trim();
            }
        }
        return null;
    }
}
