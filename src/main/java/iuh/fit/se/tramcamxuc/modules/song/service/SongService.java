package iuh.fit.se.tramcamxuc.modules.song.service;

import iuh.fit.se.tramcamxuc.modules.song.dto.request.UploadSongRequest;
import iuh.fit.se.tramcamxuc.modules.song.dto.response.SongResponse;
import iuh.fit.se.tramcamxuc.modules.song.entity.enums.SongStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;

public interface SongService {
    SongResponse uploadSong(UploadSongRequest request, MultipartFile audioFile, MultipartFile coverFile);
    Page<SongResponse> getAdminSongs(String keyword, SongStatus status, Pageable pageable);
    void approveSong(UUID songId);
    void rejectSong(UUID songId, String reason);
    Page<SongResponse> getSongsByStatusForAdmin(SongStatus status, int page, int size);
    void recordListen(UUID songId);
}
