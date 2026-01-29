package iuh.fit.se.tramcamxuc.modules.artist.service;

import iuh.fit.se.tramcamxuc.modules.artist.dto.request.RegisterArtistRequest;
import iuh.fit.se.tramcamxuc.modules.artist.dto.request.UpdateArtistRequest;
import iuh.fit.se.tramcamxuc.modules.artist.dto.response.ArtistResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.CompletableFuture;

public interface ArtistService {
    ArtistResponse registerArtist(RegisterArtistRequest request);
    ArtistResponse getMyProfile();
    ArtistResponse getPublicProfile(String artistName);
    ArtistResponse updateProfile(UpdateArtistRequest request);
    CompletableFuture<ArtistResponse> updateAvatar(MultipartFile file);
    CompletableFuture<ArtistResponse> updateCover(MultipartFile file);
}
