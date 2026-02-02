package iuh.fit.se.tramcamxuc.modules.playlist.service;

import iuh.fit.se.tramcamxuc.modules.playlist.dto.request.AddSongRequest;
import iuh.fit.se.tramcamxuc.modules.playlist.dto.request.CreatePlaylistRequest;
import iuh.fit.se.tramcamxuc.modules.playlist.dto.request.UpdatePlaylistRequest;
import iuh.fit.se.tramcamxuc.modules.playlist.dto.response.PlaylistResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface PlaylistService {

    // 1. Tạo Playlist (Có thể upload ảnh bìa luôn)
    PlaylistResponse createPlaylist(CreatePlaylistRequest request, MultipartFile thumbnail);

    // 2. Sửa Playlist (Tên, mô tả, ảnh, public/private)
    PlaylistResponse updatePlaylist(UUID playlistId, UpdatePlaylistRequest request, MultipartFile thumbnail);

    // 3. Xóa Playlist (Chỉ owner mới được xóa)
    void deletePlaylist(UUID playlistId);

    // 4. Thêm bài vào Playlist (Logic: append vào cuối)
    void addSongToPlaylist(UUID playlistId, AddSongRequest request);

    // 5. Xóa bài khỏi Playlist
    void removeSongFromPlaylist(UUID playlistId, UUID songId);

    // 6. Lấy danh sách Playlist của tôi
    Page<PlaylistResponse> getMyPlaylists(Pageable pageable);


    // --- PUBLIC ACTION (Ai cũng xem được) ---

    // 7. Xem chi tiết Playlist (kèm list bài hát)
    PlaylistResponse getPlaylistDetail(UUID id);

    // 8. Xem chi tiết bằng Slug
    PlaylistResponse getPlaylistBySlug(String slug);

    // 9. Khám phá (Lấy các playlist public)
    Page<PlaylistResponse> getPublicPlaylists(Pageable pageable);
}