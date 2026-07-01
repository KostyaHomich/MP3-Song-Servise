package com.example.resource.service;

import com.example.resource.client.SongServiceClient;
import com.example.resource.dto.DeleteIdsResponse;
import com.example.resource.dto.ResourceIdResponse;
import com.example.resource.dto.SongMetadataDto;
import com.example.resource.exception.InvalidMp3Exception;
import com.example.resource.exception.ResourceNotFoundException;
import com.example.resource.model.Resource;
import com.example.resource.repository.ResourceRepository;
import com.example.resource.validation.IdParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
            throw new InvalidMp3Exception("Invalid file format: empty. Only MP3 files are allowed");
        }

        SongMetadataDto metadata = metadataExtractor.extract(data);

        Resource resource = Resource.builder()
                .data(data)
                .name(metadata.getName())
                .build();

        Resource saved = resourceRepository.save(resource);
        log.info("Saved resource with id={}", saved.getId());

        metadata.setId(saved.getId());
        songServiceClient.createSong(metadata);

        return new ResourceIdResponse(saved.getId());
    }

    @Transactional(readOnly = true)
    public byte[] getResourceData(String rawId) {
        long id = IdParser.parsePathId(rawId);
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource with ID=" + id + " not found"));
        return resource.getData();
    }

    @Transactional
    public DeleteIdsResponse deleteResources(String idsParam) {
        List<Long> requestedIds = IdParser.parseCsvIds(idsParam);

        List<Resource> existing = resourceRepository.findAllByIdIn(requestedIds);
        List<Long> existingIds = existing.stream().map(Resource::getId).toList();

        if (!existingIds.isEmpty()) {
            resourceRepository.deleteAllById(existingIds);
            log.info("Deleted resources with ids={}", existingIds);
            songServiceClient.deleteSongs(existingIds);
        }

        return new DeleteIdsResponse(existingIds);
    }
}
