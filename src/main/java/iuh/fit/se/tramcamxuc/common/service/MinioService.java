package iuh.fit.se.tramcamxuc.common.service;

import io.minio.*;
import io.minio.http.Method;
import iuh.fit.se.tramcamxuc.common.exception.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class MinioService {

    private final MinioClient minioClient;

    @Value("${minio.bucket.music}")
    private String bucketName;

    @Value("${minio.public-url}")
    private String publicUrl;

    @Value("${minio.endpoint}")
    private String internalEndpoint;

    @Async
    public CompletableFuture<String> uploadMusicFileAsync(File file, String contentType, String originalFilename) {
        return CompletableFuture.supplyAsync(() -> {
            try (FileInputStream inputStream = new FileInputStream(file)) {
                String fileName = UUID.randomUUID() + "_" + originalFilename.replaceAll("\\s+", "_");

                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(fileName)
                                .stream(inputStream, file.length(), -1)
                                .contentType(contentType)
                                .build()
                );

                String fileUrl = String.format("%s/%s/%s", publicUrl, bucketName, fileName);
                log.info("Upload nhạc thành công: {}", fileUrl);
                return fileUrl;

            } catch (Exception e) {
                log.error("Lỗi upload nhạc: {}", e.getMessage());
                throw new RuntimeException("Upload failed");
            } finally {
                if (file != null && file.exists()) {
                    file.delete();
                }
            }
        });
    }

    @Async
    public void deleteFileAsync(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) return;
        try {
            String fileName = extractObjectNameFromUrl(fileUrl);
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );
            log.info("Đã xóa file MinIO: {}", fileName);
        } catch (Exception e) {
            log.error("Lỗi xóa file MinIO: {}", e.getMessage());
        }
    }

    //  Worker HLS Streaming

    public File downloadFile(String fileUrl, File destDir) {
        try {
            String objectName = extractObjectNameFromUrl(fileUrl);
            File destFile = new File(destDir, objectName);

            log.info("Đang tải file từ MinIO: {} -> {}", objectName, destFile.getAbsolutePath());

            minioClient.downloadObject(
                    DownloadObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .filename(destFile.getAbsolutePath())
                            .build()
            );

            return destFile;
        } catch (Exception e) {
            log.error("Lỗi download file từ MinIO: {}", e.getMessage());
            throw new RuntimeException("Không thể tải file gốc về để xử lý: " + e.getMessage());
        }
    }

    public String uploadLocalFile(File file, String objectName, String contentType) {
        try {
            minioClient.uploadObject(
                    UploadObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .filename(file.getAbsolutePath())
                            .contentType(contentType)
                            .build()
            );

            return String.format("%s/%s/%s", publicUrl, bucketName, objectName);
        } catch (Exception e) {
            log.error("Lỗi upload file local lên MinIO: {}", e.getMessage());
            throw new RuntimeException("Upload file HLS thất bại");
        }
    }

    public String uploadFolder(File folder, String remotePrefix) {
        File[] files = folder.listFiles();
        if (files == null) return null;

        String m3u8Url = null;

        for (File file : files) {
            if (file.isDirectory()) continue;

            String objectName = remotePrefix + "/" + file.getName();
            String contentType = "application/octet-stream";

            if (file.getName().endsWith(".m3u8")) {
                contentType = "application/x-mpegURL";
            } else if (file.getName().endsWith(".ts")) {
                contentType = "video/MP2T";
            }

            String url = uploadLocalFile(file, objectName, contentType);

            if (file.getName().endsWith(".m3u8")) {
                m3u8Url = url;
            }
        }
        return m3u8Url;
    }

    private String extractObjectNameFromUrl(String url) {
        String publicPrefix = publicUrl + "/" + bucketName + "/";
        if (url.startsWith(publicPrefix)) {
            return url.substring(publicPrefix.length());
        }

        String internalPrefix = internalEndpoint + "/" + bucketName + "/";
        if (url.startsWith(internalPrefix)) {
            return url.substring(internalPrefix.length());
        }

        return url.substring(url.lastIndexOf("/") + 1);
    }

    public String getPresignedDownloadUrl(String objectName, int expirySeconds) {
        try {
            String name = objectName.substring(objectName.lastIndexOf("/") + 1);

            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(name)
                            .expiry(expirySeconds, TimeUnit.SECONDS)
                            .extraQueryParams(Map.of("response-content-disposition", "attachment; filename=\"" + name + "\""))
                            .build()
            );
        } catch (Exception e) {
            throw new AppException("Lỗi MinIO: " + e.getMessage());
        }
    }
}