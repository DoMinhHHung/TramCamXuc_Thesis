package iuh.fit.se.tramcamxuc.modules.advertisement.service.impl;

import iuh.fit.se.tramcamxuc.common.exception.AppException;
import org.mp4parser.boxes.iso14496.part12.MovieHeaderBox;
import iuh.fit.se.tramcamxuc.common.service.MinioService;
import iuh.fit.se.tramcamxuc.modules.advertisement.dto.request.UploadAdRequest;
import iuh.fit.se.tramcamxuc.modules.advertisement.dto.response.AdResponse;
import iuh.fit.se.tramcamxuc.modules.advertisement.entity.Advertisement;
import iuh.fit.se.tramcamxuc.modules.advertisement.repository.AdvertisementRepository;
import iuh.fit.se.tramcamxuc.modules.advertisement.service.AdvertisementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.mp4parser.IsoFile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdvertisementServiceImpl implements AdvertisementService {

    private final AdvertisementRepository adRepository;
    private final MinioService minioService;

    @Override
    @Transactional
    public AdResponse uploadAdvertisement(UploadAdRequest request) {
        log.info("Bắt đầu upload quảng cáo: {}", request.getTitle());

        File tempFile = null;
        int duration = 0;
        try {
            tempFile = saveMultipartToTemp(request.getFile());

            duration = getDurationFromTempFile(tempFile, request.getFile().getOriginalFilename());

        } catch (Exception e) {
            log.error("Lỗi xử lý file quảng cáo: {}", e.getMessage());
            throw new AppException("Lỗi xử lý file: " + e.getMessage());
        }

        String objectNameRaw = "ads_raw/" + System.currentTimeMillis() + "_" + request.getFile().getOriginalFilename();
        String rawUrl = minioService.uploadLocalFile(tempFile, objectNameRaw, request.getFile().getContentType());

        Advertisement ad = Advertisement.builder()
                .title(request.getTitle())
                .sponsorName(request.getSponsorName())
                .clickUrl(request.getClickUrl())
                .rawUrl(rawUrl)
                .duration(duration)
                .active(false)
                .build();

        Advertisement savedAd = adRepository.save(ad);

        File finalTempFile = tempFile;
        CompletableFuture.runAsync(() -> processHlsTranscode(savedAd.getId(), finalTempFile));

        return AdResponse.fromEntity(savedAd);
    }

    @Override
    public AdResponse getRandomAdvertisement() {
        //
        return adRepository.findRandomActiveAd()
                .map(AdResponse::fromEntity)
                .orElse(null);
    }

    // --- HELPERS METHOD ---


    private void processHlsTranscode(UUID adId, File inputFile) {
        log.info(">>> Bắt đầu Transcode HLS cho Ad ID: {}", adId);
        File outputDir = null;
        try {
            outputDir = Files.createTempDirectory("ad_hls_" + adId).toFile();
            String m3u8File = outputDir.getAbsolutePath() + File.separator + "playlist.m3u8";

            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg", "-i", inputFile.getAbsolutePath(),
                    "-c:a", "aac", "-b:a", "128k",
                    "-start_number", "0",
                    "-hls_time", "10",
                    "-hls_list_size", "0",
                    "-f", "hls",
                    m3u8File
            );

            pb.redirectErrorStream(true);
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.debug("[FFmpeg] {}", line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("FFmpeg exited with code " + exitCode);
            }

            String remotePrefix = "ads_hls/" + adId;
            String masterPlaylistUrl = minioService.uploadFolder(outputDir, remotePrefix);

            // Update DB thành công
            Advertisement ad = adRepository.findById(adId).orElseThrow();
            ad.setAudioUrl(masterPlaylistUrl);
            ad.setActive(true);
            adRepository.save(ad);

            log.info(">>> Transcode thành công Ad ID: {}", adId);

        } catch (Exception e) {
            log.error(">>> Transcode thất bại cho Ad ID {}: {}", adId, e.getMessage());
        } finally {
            deleteFileOrFolder(inputFile);
            deleteFileOrFolder(outputDir);
        }
    }

    private int getDurationFromTempFile(File file, String originalFilename) {
        try {
            String extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();

            if (!Set.of(".mp3", ".wav", ".flac", ".m4a", ".mp4", ".ogg").contains(extension)) {
                throw new AppException("Định dạng file không được hỗ trợ để tính duration: " + extension);
            }

            if (".mp4".equals(extension) || ".m4a".equals(extension)) {
                try (IsoFile isoFile = new IsoFile(file)) {
                    MovieHeaderBox mvhd = isoFile.getMovieBox().getMovieHeaderBox();
                    return (int) (mvhd.getDuration() / mvhd.getTimescale());
                }
            } else {
                AudioFile audioFile = AudioFileIO.read(file);
                return audioFile.getAudioHeader().getTrackLength();
            }
        } catch (Exception e) {
            log.warn("Không tính được duration: {}", e.getMessage());
            return 0;
        }
    }

    private File saveMultipartToTemp(MultipartFile multipartFile) throws Exception {
        String ext = Objects.requireNonNull(multipartFile.getOriginalFilename()).substring(multipartFile.getOriginalFilename().lastIndexOf("."));
        File tempFile = File.createTempFile("ad_upload_", ext);
        try (var is = multipartFile.getInputStream()) {
            Files.copy(is, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        return tempFile;
    }

    private void deleteFileOrFolder(File file) {
        if (file == null || !file.exists()) return;
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) deleteFileOrFolder(f);
            }
        }
        file.delete();
    }
}