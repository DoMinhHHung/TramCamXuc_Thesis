package iuh.fit.se.tramcamxuc.modules.album.service;

import iuh.fit.se.tramcamxuc.modules.album.dto.request.AddSongToAlbumRequest;
import iuh.fit.se.tramcamxuc.modules.album.dto.request.CreateAlbumRequest;
import iuh.fit.se.tramcamxuc.modules.album.dto.request.UpdateAlbumRequest;
import iuh.fit.se.tramcamxuc.modules.album.dto.response.AlbumResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface AlbumService {

    // ================= ARTIST (Manage) =================

    // 1. Tạo Album (Có thể add luôn bài hát Draft vào)
    AlbumResponse createAlbum(CreateAlbumRequest request, MultipartFile coverFile);

    // 2. Sửa thông tin Album
    AlbumResponse updateAlbum(UUID albumId, UpdateAlbumRequest request, MultipartFile coverFile);

    // 3. Thêm bài hát vào Album (Chỉ bài của chính Artist đó)
    void addSongsToAlbum(UUID albumId, AddSongToAlbumRequest request);

    // 4. Xóa bài hát khỏi Album (Bắn bài hát ra ngoài làm Single, không xóa bài hát)
    void removeSongFromAlbum(UUID albumId, UUID songId);

    // 5. Xóa Album (Chỉ xóa vỏ, bài hát bên trong thành Single / Hoặc xóa luôn tùy business - Ở đây chọn xóa vỏ thôi cho an toàn)
    void deleteAlbum(UUID albumId);

    // 6. Gửi duyệt Album (Chuyển tất cả bài DRAFT trong album -> PENDING_APPROVAL)
    void submitAlbumForApproval(UUID albumId);

    // 7. Lấy danh sách Album của tôi
    Page<AlbumResponse> getMyAlbums(Pageable pageable);


    // ================= PUBLIC / USER (View) =================

    // 8. Xem chi tiết Album
    AlbumResponse getAlbumDetail(UUID albumId);

    // 9. Xem danh sách Album của 1 ca sĩ bất kỳ
    Page<AlbumResponse> getAlbumsByArtist(UUID artistId, Pageable pageable);
}