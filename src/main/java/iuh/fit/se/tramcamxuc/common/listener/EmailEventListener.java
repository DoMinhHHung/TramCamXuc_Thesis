package iuh.fit.se.tramcamxuc.common.listener;

import iuh.fit.se.tramcamxuc.common.event.*;
import iuh.fit.se.tramcamxuc.common.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailEventListener {

    private final EmailService emailService;

    @Async("taskExecutor")
    @EventListener
    public void handleSongApproved(SongApprovedEvent event) {
        log.info("Handling SongApprovedEvent for songId: {}", event.getSongId());
        try {
            emailService.sendSongStatusEmail(
                event.getArtistEmail(),
                event.getArtistName(),
                event.getSongTitle(),
                "APPROVED",
                event.getSongSlug()
            );
        } catch (Exception e) {
            log.error("Failed to send song approved email for songId {}: {}", event.getSongId(), e.getMessage());
        }
    }

    @Async("taskExecutor")
    @EventListener
    public void handleSongRejected(SongRejectedEvent event) {
        log.info("Handling SongRejectedEvent for songId: {}", event.getSongId());
        try {
            emailService.sendSongStatusEmail(
                event.getArtistEmail(),
                event.getArtistName(),
                event.getSongTitle(),
                "REJECTED",
                event.getReason()
            );
        } catch (Exception e) {
            log.error("Failed to send song rejected email for songId {}: {}", event.getSongId(), e.getMessage());
        }
    }

    @Async("taskExecutor")
    @EventListener
    public void handleAlbumApproved(AlbumApprovedEvent event) {
        log.info("Handling AlbumApprovedEvent for albumId: {}", event.getAlbumId());
        try {
            emailService.sendAlbumApprovedEmail(
                event.getArtistEmail(),
                event.getArtistName(),
                event.getAlbumTitle(),
                event.getAlbumCover(),
                event.getSongCount(),
                event.getReleaseDate(),
                event.getStatus(),
                event.getAlbumSlug()
            );
        } catch (Exception e) {
            log.error("Failed to send album approved email for albumId {}: {}", event.getAlbumId(), e.getMessage());
        }
    }

    @Async("taskExecutor")
    @EventListener
    public void handleAlbumRejected(AlbumRejectedEvent event) {
        log.info("Handling AlbumRejectedEvent for albumId: {}", event.getAlbumId());
        try {
            emailService.sendAlbumRejectedEmail(
                event.getArtistEmail(),
                event.getArtistName(),
                event.getAlbumTitle(),
                event.getAlbumCover(),
                event.getSongCount(),
                event.getSubmittedDate(),
                event.getReason(),
                event.getAlbumIdStr()
            );
        } catch (Exception e) {
            log.error("Failed to send album rejected email for albumId {}: {}", event.getAlbumId(), e.getMessage());
        }
    }

    @Async("taskExecutor")
    @EventListener
    public void handleUserRegistered(UserRegisteredEvent event) {
        log.info("Handling UserRegisteredEvent for email: {}", event.getEmail());
        try {
            emailService.sendHtmlEmail(
                event.getEmail(),
                "Xác thực tài khoản Trạm Cảm Xúc",
                "email/register-otp",
                Map.of("name", event.getFullName(), "otp", event.getOtp())
            );
        } catch (Exception e) {
            log.error("Failed to send registration email to {}: {}", event.getEmail(), e.getMessage());
        }
    }

    @Async("taskExecutor")
    @EventListener
    public void handlePasswordResetRequested(PasswordResetRequestedEvent event) {
        log.info("Handling PasswordResetRequestedEvent for email: {}", event.getEmail());
        try {
            emailService.sendHtmlEmail(
                event.getEmail(),
                "Đặt lại mật khẩu Trạm Cảm Xúc",
                "email/forgot-password",
                Map.of("name", event.getFullName(), "otp", event.getOtp())
            );
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}", event.getEmail(), e.getMessage());
        }
    }

    @Async("taskExecutor")
    @EventListener
    public void handlePasswordChangeRequested(PasswordChangeRequestedEvent event) {
        log.info("Handling PasswordChangeRequestedEvent for email: {}", event.getEmail());
        try {
            emailService.sendHtmlEmail(
                event.getEmail(),
                "OTP Đổi mật khẩu Phazel Sound",
                "email/change-password-otp",
                Map.of("name", event.getFullName(), "otp", event.getOtp())
            );
        } catch (Exception e) {
            log.error("Failed to send password change OTP email to {}: {}", event.getEmail(), e.getMessage());
        }
    }
}
