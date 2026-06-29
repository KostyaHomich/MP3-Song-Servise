package com.example.resource.client;

import com.example.resource.dto.SongMetadataDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class SongServiceClient {

    private final String songServiceBaseUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public SongServiceClient(
            @Value("${song-service.base-url}") String songServiceBaseUrl,
            ObjectMapper objectMapper) {
        this.songServiceBaseUrl = songServiceBaseUrl;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = objectMapper;
    }

    public void createSong(SongMetadataDto dto) {
        try {
            String body = objectMapper.writeValueAsString(dto);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(songServiceBaseUrl + "/songs"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.error("Song Service returned error {} when creating song for resource id={}",
                        response.statusCode(), dto.getId());
                throw new RuntimeException("Song Service error: " + response.body());
            }

            log.info("Successfully created song metadata for resource id={}", dto.getId());
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Failed to call Song Service for resource id={}", dto.getId(), ex);
            throw new RuntimeException("Failed to communicate with Song Service", ex);
        }
    }

    public void deleteSongs(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        try {
            String idsParam = ids.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(songServiceBaseUrl + "/songs?id=" + idsParam))
                    .DELETE()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.error("Song Service returned error {} when deleting songs ids={}",
                        response.statusCode(), ids);
            } else {
                log.info("Successfully deleted song metadata for resource ids={}", ids);
            }
        } catch (Exception ex) {
            log.error("Failed to call Song Service delete for ids={}", ids, ex);
        }
    }
}
