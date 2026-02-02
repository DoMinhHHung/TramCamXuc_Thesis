package iuh.fit.se.tramcamxuc.modules.playlist.repository;

import iuh.fit.se.tramcamxuc.modules.playlist.entity.PlaylistSong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlaylistSongRepository extends JpaRepository<PlaylistSong, UUID> {

    @Query("SELECT COALESCE(MAX(ps.order), 0) FROM PlaylistSong ps WHERE ps.playlist.id = :playlistId")
    int findMaxOrderByPlaylistId(@Param("playlistId") UUID playlistId);

    Optional<PlaylistSong> findByPlaylistIdAndSongId(UUID playlistId, UUID songId);

    boolean existsByPlaylistIdAndSongId(UUID playlistId, UUID songId);
}