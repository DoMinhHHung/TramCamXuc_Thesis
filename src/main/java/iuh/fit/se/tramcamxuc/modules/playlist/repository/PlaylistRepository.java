package iuh.fit.se.tramcamxuc.modules.playlist.repository;

import iuh.fit.se.tramcamxuc.modules.playlist.entity.Playlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, UUID> {

    boolean existsBySlug(String slug);

    Page<Playlist> findByUserId(UUID userId, Pageable pageable);

    Page<Playlist> findByIsPublicTrue(Pageable pageable);

    @Query("SELECT p FROM Playlist p " +
            "LEFT JOIN FETCH p.playlistSongs ps " +
            "LEFT JOIN FETCH ps.song s " +
            "LEFT JOIN FETCH s.artist " +
            "WHERE p.id = :id " +
            "ORDER BY ps.order ASC")
    Optional<Playlist> findByIdWithSongs(@Param("id") UUID id);

    @Query("SELECT p FROM Playlist p " +
            "LEFT JOIN FETCH p.playlistSongs ps " +
            "LEFT JOIN FETCH ps.song s " +
            "LEFT JOIN FETCH s.artist " +
            "WHERE p.slug = :slug " +
            "ORDER BY ps.order ASC")
    Optional<Playlist> findBySlugWithSongs(@Param("slug") String slug);
}