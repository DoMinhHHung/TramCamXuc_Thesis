package iuh.fit.se.tramcamxuc.common.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.base-url}")
    private String baseUrl;

    @Async("taskExecutor")
    public void sendHtmlEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());

            Context context = new Context();
            context.setVariables(variables);
            String html = templateEngine.process(templateName, context);

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);

            mailSender.send(message);
            log.info("Email sent to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}", to, e);
        }
    }

    @Async("taskExecutor")
    public void sendSongStatusEmail(String to, String userName, String songTitle, String status, String reasonOrSlug) {
        try {
            String subject = status.equals("APPROVED") ? "🎉 Bài hát của bạn đã được duyệt!" : "⚠️ Thông báo về bài hát của bạn";
            String templateName = status.equals("APPROVED") ? "email/song-approved" : "email/song-rejected";

            Context context = new Context();
            context.setVariable("name", userName);
            context.setVariable("songTitle", songTitle);

            if (status.equals("APPROVED")) {
                String songSlug = reasonOrSlug;
                String deepLink = "tramcamxuc://song/" + songSlug;
                context.setVariable("deepLink", deepLink);
                String webLink = baseUrl + "/song/" + songSlug;
                context.setVariable("webLink", webLink);
            } else {
                context.setVariable("reason", reasonOrSlug);
            }

            String htmlContent = templateEngine.process(templateName, context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Sent song status email to {}", to);

        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    @Async("taskExecutor")
    public void sendAlbumApprovedEmail(String to, String artistName, String albumTitle, String albumCover, 
                                       int songCount, String releaseDate, String status, String albumSlug) {
        try {
            String subject = "🎉 Album của bạn đã được duyệt!";
            String templateName = "email/album-approved";

            Context context = new Context();
            context.setVariable("artistName", artistName);
            context.setVariable("albumTitle", albumTitle);
            context.setVariable("albumCover", albumCover != null ? albumCover : "");
            context.setVariable("songCount", songCount);
            context.setVariable("releaseDate", releaseDate);
            context.setVariable("status", status);
            
            String albumLink = baseUrl + "/album/" + albumSlug;
            String dashboardLink = baseUrl + "/artist/dashboard";
            context.setVariable("albumLink", albumLink);
            context.setVariable("dashboardLink", dashboardLink);

            String htmlContent = templateEngine.process(templateName, context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Sent album approved email to {} for album: {}", to, albumTitle);

        } catch (Exception e) {
            log.error("Failed to send album approved email to {}: {}", to, e.getMessage());
        }
    }

    @Async("taskExecutor")
    public void sendAlbumRejectedEmail(String to, String artistName, String albumTitle, String albumCover,
                                       int songCount, String submittedDate, String reason, String albumId) {
        try {
            String subject = "⚠️ Thông báo về Album của bạn";
            String templateName = "email/album-rejected";

            Context context = new Context();
            context.setVariable("artistName", artistName);
            context.setVariable("albumTitle", albumTitle);
            context.setVariable("albumCover", albumCover != null ? albumCover : "");
            context.setVariable("songCount", songCount);
            context.setVariable("submittedDate", submittedDate);
            context.setVariable("reason", reason);

            String editAlbumLink = baseUrl + "/artist/albums/" + albumId + "/edit";
            String supportLink = baseUrl + "/support";
            context.setVariable("editAlbumLink", editAlbumLink);
            context.setVariable("supportLink", supportLink);

            String htmlContent = templateEngine.process(templateName, context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Sent album rejected email to {} for album: {}", to, albumTitle);

        } catch (Exception e) {
            log.error("Failed to send album rejected email to {}: {}", to, e.getMessage());
        }
    }
}