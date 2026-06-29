package com.example.resource.service;

import com.example.resource.client.SongServiceClient;
import com.example.resource.dto.DeleteIdsResponse;
import com.example.resource.dto.ResourceIdResponse;
import com.example.resource.dto.SongMetadataDto;
import com.example.resource.exception.InvalidMp3Exception;
import com.example.resource.exception.ResourceNotFoundException;
import com.example.resource.model.Resource;
import com.example.resource.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final Mp3MetadataExtractor metadataExtractor;
    private final SongServiceClient songServiceClient;

    @Transactional
    public ResourceIdResponse uploadResource(byte[] data) {
        if (data == null || data.length == 0) {
            throw new InvalidMp3Exception("Request body is empty or missing");
        }

        // Extract metadata (validates MP3 format)
        SongMetadataDto metadata = metadataExtractor.extract(data);

        // Save resource
        Resource resource = Resource.builder()
                .data(data)
                .name(metadata.getName())
                .build();

        Resource saved = resourceRepository.save(resource);
        log.info("Saved resource with id={}", saved.getId());

        // Set the resource ID on the metadata and call Song Service
        metadata.setId(saved.getId());
        songServiceClient.createSong(metadata);

        return new ResourceIdResponse(saved.getId());
    }

    @Transactional(readOnly = true)
    public byte[] getResourceData(Long id) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with id: " + id));
        return resource.getData();
    }

    @Transactional
    public DeleteIdsResponse deleteResources(String idsParam) {
        if (idsParam == null || idsParam.isBlank()) {
            throw new IllegalArgumentException("Query parameter 'id' must not be empty");
        }

        if (idsParam.length() > 200) {
            throw new IllegalArgumentException(
                    "Query parameter 'id' exceeds maximum allowed length of 200 characters");
        }

        List<Long> requestedIds;
        try {
            requestedIds = Arrays.stream(idsParam.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::parseLong)
                    .toList();
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    "Invalid ID format in query parameter 'id': " + idsParam);
        }

        if (requestedIds.isEmpty()) {
            throw new IllegalArgumentException("No valid IDs provided");
        }

        // Find only existing resources
        List<Resource> existing = resourceRepository.findAllByIdIn(requestedIds);
        List<Long> existingIds = existing.stream().map(Resource::getId).toList();

        if (!existingIds.isEmpty()) {
            resourceRepository.deleteAllById(existingIds);
            log.info("Deleted resources with ids={}", existingIds);

            // Cascade delete to Song Service
            songServiceClient.deleteSongs(existingIds);
        }

        return new DeleteIdsResponse(existingIds);
    }
}
