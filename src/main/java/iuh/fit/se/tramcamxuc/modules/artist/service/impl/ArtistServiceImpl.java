package iuh.fit.se.tramcamxuc.modules.artist.service.impl;

import iuh.fit.se.tramcamxuc.common.exception.AppException;
import iuh.fit.se.tramcamxuc.common.exception.ResourceNotFoundException;
import iuh.fit.se.tramcamxuc.common.service.CloudinaryService;
import iuh.fit.se.tramcamxuc.modules.artist.dto.request.RegisterArtistRequest;
import iuh.fit.se.tramcamxuc.modules.artist.dto.request.UpdateArtistRequest;
import iuh.fit.se.tramcamxuc.modules.artist.dto.response.ArtistResponse;
import iuh.fit.se.tramcamxuc.modules.artist.entity.Artist;
import iuh.fit.se.tramcamxuc.modules.artist.repository.ArtistRepository;
import iuh.fit.se.tramcamxuc.modules.artist.service.ArtistService;
import iuh.fit.se.tramcamxuc.modules.subscription.entity.UserSubscription;
import iuh.fit.se.tramcamxuc.modules.subscription.entity.enums.SubscriptionStatus;
import iuh.fit.se.tramcamxuc.modules.subscription.repository.UserSubscriptionRepository;
import iuh.fit.se.tramcamxuc.modules.user.entity.User;
import iuh.fit.se.tramcamxuc.modules.user.entity.enums.Role;
import iuh.fit.se.tramcamxuc.modules.user.repository.UserRepository;
import iuh.fit.se.tramcamxuc.modules.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class ArtistServiceImpl implements ArtistService {
    private final UserService userService;
    private final UserRepository userRepository;
    private final ArtistRepository artistRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional
    public ArtistResponse registerArtist(RegisterArtistRequest request) {
        User user = userService.getCurrentUser();

        if (artistRepository.findByUserId(user.getId()).isPresent()) {
            throw new AppException("You are already registered as an artist. Cannot register again.");
        }

        validateArtistEligibility(user);

        if (artistRepository.existsByArtistName(request.getArtistName())) {
            throw new AppException("Stage name '" + request.getArtistName() + "' already. Please choose another one.");
        }

        // 4. Tạo Profile
        Artist artist = Artist.builder()
                .user(user)
                .artistName(request.getArtistName())
                .bio(request.getBio())
                .facebookUrl(request.getFacebookUrl())
                .instagramUrl(request.getInstagramUrl())
                .youtubeUrl(request.getYoutubeUrl())
                .avatarUrl(user.getAvatarUrl())
                .totalPlays(0)
                .build();

        Artist savedArtist = artistRepository.save(artist);

        user.setRole(Role.ARTIST);
        userRepository.save(user);

        return ArtistResponse.fromEntity(savedArtist);
    }

    @Override
    public ArtistResponse getMyProfile() {
        User user = userService.getCurrentUser();
        Artist artist = artistRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("You are not registered as an artist."));
        return ArtistResponse.fromEntity(artist);
    }

    @Override
    public ArtistResponse getPublicProfile(String artistName) {
        Artist artist = artistRepository.findByArtistName(artistName)
                .orElseThrow(() -> new ResourceNotFoundException("Artist profile not found: " + artistName));
        return ArtistResponse.fromEntity(artist);
    }

    @Override
    @Transactional
    public ArtistResponse updateProfile(UpdateArtistRequest request) {
        User user = userService.getCurrentUser();
        Artist artist = artistRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Profile artist not found."));

        if (request.getBio() != null) artist.setBio(request.getBio());
        if (request.getFacebookUrl() != null) artist.setFacebookUrl(request.getFacebookUrl());
        if (request.getInstagramUrl() != null) artist.setInstagramUrl(request.getInstagramUrl());
        if (request.getYoutubeUrl() != null) artist.setYoutubeUrl(request.getYoutubeUrl());

        if (request.getArtistName() != null && !request.getArtistName().equals(artist.getArtistName())) {
            if (artistRepository.existsByArtistName(request.getArtistName())) {
                throw new AppException("This stage name '" + request.getArtistName() + "' is already taken. Please choose another one.");
            }
            artist.setArtistName(request.getArtistName());
        }

        return ArtistResponse.fromEntity(artistRepository.save(artist));
    }

    @Override
    @Transactional
    public CompletableFuture<ArtistResponse> updateAvatar(MultipartFile file) {
        User user = userService.getCurrentUser();
        Artist artist = artistRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Artist profile not found"));

        return cloudinaryService.uploadImageAsync(file, "tramcamxuc/artists/avatars")
                .thenApply(newAvatarUrl -> {
                    if (artist.getAvatarUrl() != null) {
                        cloudinaryService.deleteImage(artist.getAvatarUrl());
                    }

                    artist.setAvatarUrl(newAvatarUrl);
                    Artist savedArtist = artistRepository.save(artist);
                    return ArtistResponse.fromEntity(savedArtist);
                });
    }

    @Override
    @Transactional
    public CompletableFuture<ArtistResponse> updateCover(MultipartFile file) {
        User user = userService.getCurrentUser();
        Artist artist = artistRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Artist profile not found"));

        return cloudinaryService.uploadImageAsync(file, "tramcamxuc/artists/covers")
                .thenApply(newCoverUrl -> {
                    if (artist.getCoverUrl() != null) {
                        cloudinaryService.deleteImage(artist.getCoverUrl());
                    }

                    artist.setCoverUrl(newCoverUrl);
                    Artist savedArtist = artistRepository.save(artist);
                    return ArtistResponse.fromEntity(savedArtist);
                });
    }

    private void validateArtistEligibility(User user) {
        UserSubscription subscription = userSubscriptionRepository
                .findByUserIdAndStatus(user.getId(), SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new AppException("Bạn chưa đăng ký gói cước nào. Vui lòng mua gói Artist."));

        if (subscription.getPlan().getFeatures() == null ||
                !subscription.getPlan().getFeatures().isCanBecomeArtist()) {
            throw new AppException("Subscription plan does not allow artist registration. Please upgrade your plan.");
        }
    }
}
