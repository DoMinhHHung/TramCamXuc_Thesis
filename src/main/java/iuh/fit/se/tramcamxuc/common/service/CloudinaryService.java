package iuh.fit.se.tramcamxuc.common.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryService {
    private final Cloudinary cloudinary;

    @Async
    public CompletableFuture<String> uploadAvatarAsync(MultipartFile file, String userId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String publicId = "avatar_" + userId;
                Map params = ObjectUtils.asMap(
                        "folder", "tramcamxuc/avatars",
                        "public_id", publicId,
                        "overwrite", true,
                        "invalidate", true,
                        "resource_type", "image",
                        "transformation", new Transformation()
                                .width(500).height(500).crop("fill").gravity("face")
                );

                Map uploadResult = cloudinary.uploader().upload(file.getBytes(), params);
                String url = uploadResult.get("secure_url").toString();

                log.info("Upload avatar thành công cho user {}: {}", userId, url);
                return url;

            } catch (IOException e) {
                log.error("Lỗi upload avatar cho user {}: {}", userId, e.getMessage());
                throw new RuntimeException("Upload ảnh thất bại: " + e.getMessage());
            }
        });
    }

    @Async
    public CompletableFuture<String> uploadImageAsync(MultipartFile file, String folderName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Map params = ObjectUtils.asMap(
                        "folder", folderName,
                        "resource_type", "image"
                );
                Map uploadResult = cloudinary.uploader().upload(file.getBytes(), params);
                return uploadResult.get("secure_url").toString();
            } catch (IOException e) {
                throw new RuntimeException("Lỗi upload ảnh: " + e.getMessage());
            }
        });
    }

    @Async
    public void deleteImage(String imageUrl) {
        if (imageUrl == null) return;
        try {

            String[] parts = imageUrl.split("/");
            String publicIdWithExtension = "";

            int uploadIndex = imageUrl.indexOf("upload/");
            if (uploadIndex != -1) {
                String suffix = imageUrl.substring(uploadIndex + 7);
                if (suffix.startsWith("v")) {
                    int slashIndex = suffix.indexOf("/");
                    if (slashIndex != -1) {
                        suffix = suffix.substring(slashIndex + 1);
                    }
                }
                publicIdWithExtension = suffix;
            }

            String publicId = publicIdWithExtension;
            if (publicId.contains(".")) {
                publicId = publicId.substring(0, publicId.lastIndexOf("."));
            }

            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("Đã xóa ảnh rác Cloudinary: {}", publicId);

        } catch (Exception e) {
            log.error("Lỗi xóa ảnh Cloudinary: {}", e.getMessage());
        }
    }

    public String uploadImageFromUrl(String imageUrl, String folderName) {
        try {
            Map params = ObjectUtils.asMap(
                    "folder", folderName,
                    "resource_type", "image"
            );
            Map uploadResult = cloudinary.uploader().upload(imageUrl, params);

            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            log.error("Lỗi sync ảnh từ URL: {}", e.getMessage());
            return null;
        }
    }
}