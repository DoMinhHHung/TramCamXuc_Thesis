package iuh.fit.se.tramcamxuc.modules.song.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;

@Data
public class UploadSongRequest {
    @NotBlank(message = "Tên bài hát không được để trống")
    private String title;

    private String bio;

    @NotNull(message = "Phải chọn thể loại")
    private UUID genreId;

     private MultipartFile audioFile;
     private MultipartFile coverFile;
}