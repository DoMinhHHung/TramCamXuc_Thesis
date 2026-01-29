package iuh.fit.se.tramcamxuc.modules.song.repository;

import iuh.fit.se.tramcamxuc.modules.song.entity.Song;
import iuh.fit.se.tramcamxuc.modules.song.entity.enums.SongStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SongRepository extends JpaRepository<Song, UUID> {

    @Query("SELECT s FROM Song s WHERE s.status = 'PUBLIC' AND " +
            "(LOWER(s.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.artist.artistName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Song> searchPublicSongs(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT s FROM Song s WHERE " +
            "(:status IS NULL OR s.status = :status) AND " +
            "(:keyword IS NULL OR :keyword = '' OR LOWER(s.title) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Song> findForAdmin(@Param("keyword") String keyword, @Param("status") SongStatus status, Pageable pageable);

    Page<Song> findByArtistIdAndStatusNot(UUID artistId, SongStatus status, Pageable pageable);
}