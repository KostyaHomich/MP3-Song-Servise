package com.example.resource.controller;

import com.example.resource.dto.DeleteIdsResponse;
import com.example.resource.dto.ResourceIdResponse;
import com.example.resource.service.ResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/resources")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;

    @PostMapping(consumes = "audio/mpeg")
    public ResponseEntity<ResourceIdResponse> uploadResource(@RequestBody byte[] data) {
        log.info("POST /resources - received {} bytes", data != null ? data.length : 0);
        ResourceIdResponse response = resourceService.uploadResource(data);
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/{id}", produces = "audio/mpeg")
    public ResponseEntity<byte[]> getResource(@PathVariable String id) {
        log.info("GET /resources/{}", id);

        long resourceId;
        try {
            resourceId = Long.parseLong(id);
        } catch (NumberFormatException ex) {
            return ResponseEntity.badRequest().build();
        }

        if (resourceId <= 0) {
            return ResponseEntity.badRequest().build();
        }

        byte[] data = resourceService.getResourceData(resourceId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("audio/mpeg"));
        headers.setContentLength(data.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(data);
    }

    @DeleteMapping
    public ResponseEntity<DeleteIdsResponse> deleteResources(@RequestParam("id") String id) {
        log.info("DELETE /resources?id={}", id);
        DeleteIdsResponse response = resourceService.deleteResources(id);
        return ResponseEntity.ok(response);
    }
}
