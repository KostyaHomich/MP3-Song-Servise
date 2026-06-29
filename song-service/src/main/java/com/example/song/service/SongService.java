package com.example.song.service;

import com.example.song.dto.DeleteIdsResponse;
import com.example.song.dto.SongDto;
import com.example.song.dto.SongIdResponse;
import com.example.song.exception.SongNotFoundException;
import com.example.song.model.Song;
import com.example.song.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SongService {

    private final SongRepository songRepository;

    @Transactional
    public SongIdResponse createSong(SongDto dto) {
        validateSongDto(dto);

        Song song = Song.builder()
                .id(dto.getId())
                .name(dto.getName())
                .artist(dto.getArtist())
                .album(dto.getAlbum())
                .duration(dto.getDuration())
                .year(dto.getYear())
                .build();

        Song saved = songRepository.save(song);
        log.info("Created song metadata with id={}", saved.getId());
        return new SongIdResponse(saved.getId());
    }

    @Transactional(readOnly = true)
    public SongDto getSong(Long id) {
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new SongNotFoundException("Song not found with id: " + id));

        return toDto(song);
    }

    @Transactional
    public DeleteIdsResponse deleteSongs(String idsParam) {
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

        List<Song> existing = songRepository.findAllByIdIn(requestedIds);
        List<Long> existingIds = existing.stream().map(Song::getId).toList();

        if (!existingIds.isEmpty()) {
            songRepository.deleteAllById(existingIds);
            log.info("Deleted songs with ids={}", existingIds);
        }

        return new DeleteIdsResponse(existingIds);
    }

    private void validateSongDto(SongDto dto) {
        if (dto.getId() == null) {
            throw new IllegalArgumentException("Song id must not be null");
        }
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new IllegalArgumentException("Song name must not be blank");
        }
    }

    private SongDto toDto(Song song) {
        return SongDto.builder()
                .id(song.getId())
                .name(song.getName())
                .artist(song.getArtist())
                .album(song.getAlbum())
                .duration(song.getDuration())
                .year(song.getYear())
                .build();
    }
}
