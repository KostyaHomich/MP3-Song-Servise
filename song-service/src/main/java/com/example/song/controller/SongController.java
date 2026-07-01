package com.example.song.controller;

import com.example.song.dto.DeleteIdsResponse;
import com.example.song.dto.SongDto;
import com.example.song.dto.SongIdResponse;
import com.example.song.service.SongService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/songs")
@RequiredArgsConstructor
public class SongController {

    private final SongService songService;

    @PostMapping
    public ResponseEntity<SongIdResponse> createSong(@RequestBody SongDto dto) {
        log.info("POST /songs - id={}", dto.getId());
        SongIdResponse response = songService.createSong(dto);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SongDto> getSong(@PathVariable String id) {
        log.info("GET /songs/{}", id);
        SongDto dto = songService.getSong(id);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping
    public ResponseEntity<DeleteIdsResponse> deleteSongs(@RequestParam("id") String id) {
        log.info("DELETE /songs?id={}", id);
        DeleteIdsResponse response = songService.deleteSongs(id);
        return ResponseEntity.ok(response);
    }
}
