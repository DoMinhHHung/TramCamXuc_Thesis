package iuh.fit.se.tramcamxuc.modules.artist.service.impl;

import iuh.fit.se.tramcamxuc.common.exception.AppException;
import iuh.fit.se.tramcamxuc.common.exception.ResourceNotFoundException;
import iuh.fit.se.tramcamxuc.common.service.CloudinaryService;
import iuh.fit.se.tramcamxuc.modules.artist.dto.request.RegisterArtistRequest;
import iuh.fit.se.tramcamxuc.modules.artist.dto.request.UpdateArtistRequest;
import iuh.fit.se.tramcamxuc.modules.artist.dto.response.ArtistResponse;
import iuh.fit.se.tramcamxuc.modules.artist.entity.Artist;
import iuh.fit.se.tramcamxuc.modules.artist.repository.ArtistRepository;
import iuh.fit.se.tramcamxuc.modules.subscription.entity.SubscriptionPlan;
import iuh.fit.se.tramcamxuc.modules.subscription.entity.UserSubscription;
import iuh.fit.se.tramcamxuc.modules.subscription.entity.enums.SubscriptionStatus;
import iuh.fit.se.tramcamxuc.modules.subscription.model.PlanFeatures;
import iuh.fit.se.tramcamxuc.modules.subscription.repository.UserSubscriptionRepository;
import iuh.fit.se.tramcamxuc.modules.user.entity.User;
import iuh.fit.se.tramcamxuc.modules.user.entity.enums.Role;
import iuh.fit.se.tramcamxuc.modules.user.repository.UserRepository;
import iuh.fit.se.tramcamxuc.modules.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArtistServiceImplTest {

    @Mock
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ArtistRepository artistRepository;

    @Mock
    private UserSubscriptionRepository userSubscriptionRepository;

    @Mock
    private CloudinaryService cloudinaryService;

    @InjectMocks
    private ArtistServiceImpl artistService;

    private User user;
    private Artist artist;
    private UserSubscription subscription;
    private SubscriptionPlan plan;
    private UUID userId;
    private UUID artistId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        artistId = UUID.randomUUID();

        user = User.builder()
                .email("user@test.com")
                .fullName("Test User")
                .role(Role.USER)
                .build();
        ReflectionTestUtils.setField(user, "id", userId);

        PlanFeatures features = new PlanFeatures();
        features.setCanBecomeArtist(true);

        plan = SubscriptionPlan.builder()
                .name("Premium Plan")
                .price(99000)
                .features(features)
                .build();

        subscription = UserSubscription.builder()
                .user(user)
                .plan(plan)
                .status(SubscriptionStatus.ACTIVE)
                .build();

        artist = Artist.builder()
                .user(user)
                .artistName("Test Artist")
                .bio("Test Bio")
                .totalPlays(0)
                .build();
        ReflectionTestUtils.setField(artist, "id", artistId);
    }

    @Test
    @DisplayName("Should register artist successfully")
    void registerArtist_Success() {
        // Given
        RegisterArtistRequest request = new RegisterArtistRequest();
        request.setArtistName("New Artist");
        request.setBio("Artist Bio");
        request.setAcceptTerms(true);

        when(userService.getCurrentUser()).thenReturn(user);
        when(artistRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(userSubscriptionRepository.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE))
                .thenReturn(Optional.of(subscription));
        when(artistRepository.existsByArtistName(anyString())).thenReturn(false);
        when(artistRepository.save(any(Artist.class))).thenReturn(artist);
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        ArtistResponse result = artistService.registerArtist(request);

        // Then
        assertNotNull(result);
        verify(artistRepository).save(any(Artist.class));
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when user already registered as artist")
    void registerArtist_AlreadyRegistered() {
        // Given
        RegisterArtistRequest request = new RegisterArtistRequest();
        request.setArtistName("New Artist");

        when(userService.getCurrentUser()).thenReturn(user);
        when(artistRepository.findByUserId(userId)).thenReturn(Optional.of(artist));

        // When & Then
        assertThrows(AppException.class, () -> 
            artistService.registerArtist(request)
        );
    }

    @Test
    @DisplayName("Should throw exception when artist name already exists")
    void registerArtist_NameExists() {
        // Given
        RegisterArtistRequest request = new RegisterArtistRequest();
        request.setArtistName("Existing Artist");
        request.setAcceptTerms(true);

        when(userService.getCurrentUser()).thenReturn(user);
        when(artistRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(userSubscriptionRepository.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE))
                .thenReturn(Optional.of(subscription));
        when(artistRepository.existsByArtistName("Existing Artist")).thenReturn(true);

        // When & Then
        assertThrows(AppException.class, () -> 
            artistService.registerArtist(request)
        );
    }

    @Test
    @DisplayName("Should throw exception when user has no subscription")
    void registerArtist_NoSubscription() {
        // Given
        RegisterArtistRequest request = new RegisterArtistRequest();
        request.setArtistName("New Artist");

        when(userService.getCurrentUser()).thenReturn(user);
        when(artistRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(userSubscriptionRepository.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(AppException.class, () -> 
            artistService.registerArtist(request)
        );
    }

    @Test
    @DisplayName("Should throw exception when terms not accepted")
    void registerArtist_TermsNotAccepted() {
        // Given
        RegisterArtistRequest request = new RegisterArtistRequest();
        request.setArtistName("New Artist");
        request.setAcceptTerms(false);

        when(userService.getCurrentUser()).thenReturn(user);
        when(artistRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(userSubscriptionRepository.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE))
                .thenReturn(Optional.of(subscription));
        when(artistRepository.existsByArtistName(anyString())).thenReturn(false);

        // When & Then
        assertThrows(AppException.class, () -> 
            artistService.registerArtist(request)
        );
    }

    @Test
    @DisplayName("Should get my profile successfully")
    void getMyProfile_Success() {
        // Given
        when(userService.getCurrentUser()).thenReturn(user);
        when(artistRepository.findByUserId(userId)).thenReturn(Optional.of(artist));

        // When
        ArtistResponse result = artistService.getMyProfile();

        // Then
        assertNotNull(result);
        assertEquals("Test Artist", result.getArtistName());
    }

    @Test
    @DisplayName("Should throw exception when getting profile but not artist")
    void getMyProfile_NotArtist() {
        // Given
        when(userService.getCurrentUser()).thenReturn(user);
        when(artistRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> 
            artistService.getMyProfile()
        );
    }

    @Test
    @DisplayName("Should get public profile successfully")
    void getPublicProfile_Success() {
        // Given
        when(artistRepository.findByArtistName("Test Artist")).thenReturn(Optional.of(artist));

        // When
        ArtistResponse result = artistService.getPublicProfile("Test Artist");

        // Then
        assertNotNull(result);
        assertEquals("Test Artist", result.getArtistName());
    }

    @Test
    @DisplayName("Should update profile successfully")
    void updateProfile_Success() {
        // Given
        UpdateArtistRequest request = new UpdateArtistRequest();
        request.setBio("Updated Bio");
        request.setFacebookUrl("http://facebook.com/artist");

        when(userService.getCurrentUser()).thenReturn(user);
        when(artistRepository.findByUserId(userId)).thenReturn(Optional.of(artist));
        when(artistRepository.save(any(Artist.class))).thenReturn(artist);

        // When
        ArtistResponse result = artistService.updateProfile(request);

        // Then
        assertNotNull(result);
        verify(artistRepository).save(any(Artist.class));
    }

    @Test
    @DisplayName("Should update artist name successfully")
    void updateProfile_UpdateArtistName() {
        // Given
        UpdateArtistRequest request = new UpdateArtistRequest();
        request.setArtistName("New Artist Name");

        when(userService.getCurrentUser()).thenReturn(user);
        when(artistRepository.findByUserId(userId)).thenReturn(Optional.of(artist));
        when(artistRepository.existsByArtistName("New Artist Name")).thenReturn(false);
        when(artistRepository.save(any(Artist.class))).thenReturn(artist);

        // When
        ArtistResponse result = artistService.updateProfile(request);

        // Then
        assertNotNull(result);
        verify(artistRepository).existsByArtistName("New Artist Name");
    }

    @Test
    @DisplayName("Should throw exception when updating to existing artist name")
    void updateProfile_NameExists() {
        // Given
        UpdateArtistRequest request = new UpdateArtistRequest();
        request.setArtistName("Existing Name");

        when(userService.getCurrentUser()).thenReturn(user);
        when(artistRepository.findByUserId(userId)).thenReturn(Optional.of(artist));
        when(artistRepository.existsByArtistName("Existing Name")).thenReturn(true);

        // When & Then
        assertThrows(AppException.class, () -> 
            artistService.updateProfile(request)
        );
    }

    @Test
    @DisplayName("Should update avatar successfully")
    void updateAvatar_Success() {
        // Given
        MultipartFile file = mock(MultipartFile.class);
        artist.setAvatarUrl("http://old-avatar.url");

        when(userService.getCurrentUser()).thenReturn(user);
        when(artistRepository.findByUserId(userId)).thenReturn(Optional.of(artist));
        when(cloudinaryService.uploadImageAsync(any(), anyString()))
                .thenReturn(CompletableFuture.completedFuture("http://new-avatar.url"));
        when(artistRepository.save(any(Artist.class))).thenReturn(artist);

        // When
        CompletableFuture<ArtistResponse> result = artistService.updateAvatar(file);

        // Then
        assertNotNull(result);
        verify(cloudinaryService).uploadImageAsync(any(), anyString());
        verify(cloudinaryService).deleteImage("http://old-avatar.url");
    }

    @Test
    @DisplayName("Should update cover successfully")
    void updateCover_Success() {
        // Given
        MultipartFile file = mock(MultipartFile.class);
        artist.setCoverUrl("http://old-cover.url");

        when(userService.getCurrentUser()).thenReturn(user);
        when(artistRepository.findByUserId(userId)).thenReturn(Optional.of(artist));
        when(cloudinaryService.uploadImageAsync(any(), anyString()))
                .thenReturn(CompletableFuture.completedFuture("http://new-cover.url"));
        when(artistRepository.save(any(Artist.class))).thenReturn(artist);

        // When
        CompletableFuture<ArtistResponse> result = artistService.updateCover(file);

        // Then
        assertNotNull(result);
        verify(cloudinaryService).uploadImageAsync(any(), anyString());
        verify(cloudinaryService).deleteImage("http://old-cover.url");
    }
}
