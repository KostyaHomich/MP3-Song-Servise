package com.example.song.service;

import com.example.song.dto.DeleteIdsResponse;
import com.example.song.dto.SongDto;
import com.example.song.dto.SongIdResponse;
import com.example.song.exception.SongAlreadyExistsException;
import com.example.song.exception.SongNotFoundException;
import com.example.song.model.Song;
import com.example.song.repository.SongRepository;
import com.example.song.validation.IdParser;
import com.example.song.validation.SongValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SongService {

    private final SongRepository songRepository;
    private final SongValidator songValidator;

    @Transactional
    public SongIdResponse createSong(SongDto dto) {
        songValidator.validate(dto);

        if (dto.getId() != null && songRepository.existsById(dto.getId())) {
            throw new SongAlreadyExistsException("Metadata for resource ID=" + dto.getId() + " already exists");
        }

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
    public SongDto getSong(String rawId) {
        long id = IdParser.parsePathId(rawId);
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new SongNotFoundException("Song metadata for ID=" + id + " not found"));
        return toDto(song);
    }

    @Transactional
    public DeleteIdsResponse deleteSongs(String idsParam) {
        List<Long> requestedIds = IdParser.parseCsvIds(idsParam);

        List<Song> existing = songRepository.findAllByIdIn(requestedIds);
        List<Long> existingIds = existing.stream().map(Song::getId).toList();

        if (!existingIds.isEmpty()) {
            songRepository.deleteAllById(existingIds);
            log.info("Deleted songs with ids={}", existingIds);
        }

        return new DeleteIdsResponse(existingIds);
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
