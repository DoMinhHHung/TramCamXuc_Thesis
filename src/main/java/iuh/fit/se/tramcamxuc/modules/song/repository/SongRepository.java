package iuh.fit.se.tramcamxuc.modules.song.repository;

import iuh.fit.se.tramcamxuc.modules.song.entity.Song;
import iuh.fit.se.tramcamxuc.modules.song.entity.enums.SongStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SongRepository extends JpaRepository<Song, UUID> {

    @Query("SELECT s FROM Song s " +
            "LEFT JOIN FETCH s.artist " +
            "LEFT JOIN FETCH s.genre " +
            "WHERE s.status = 'PUBLIC' AND " +
            "(LOWER(s.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.artist.artistName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Song> searchPublicSongs(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT s FROM Song s " +
            "LEFT JOIN FETCH s.artist " +
            "LEFT JOIN FETCH s.genre " +
            "WHERE s.status IN ('PENDING_APPROVAL', 'PUBLIC', 'PRIVATE', 'REJECTED') " +
            "AND (:status IS NULL OR s.status = :status) " +
            "AND (:keyword IS NULL OR :keyword = '' OR LOWER(s.title) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Song> findForAdmin(@Param("keyword") String keyword, @Param("status") SongStatus status, Pageable pageable);

    @Query("SELECT s FROM Song s " +
            "LEFT JOIN FETCH s.artist " +
            "LEFT JOIN FETCH s.genre " +
            "WHERE s.artist.id = :artistId AND s.status != :status")
    Page<Song> findByArtistIdAndStatusNot(@Param("artistId") UUID artistId, @Param("status") SongStatus status, Pageable pageable);
    
    @Query("SELECT s FROM Song s " +
            "LEFT JOIN FETCH s.artist " +
            "LEFT JOIN FETCH s.genre " +
            "WHERE s.status = :status ORDER BY s.createdAt DESC")
    Page<Song> findByStatusOrderByCreatedAtDesc(@Param("status") SongStatus status, Pageable pageable);

    @Query("SELECT COALESCE(SUM(s.playCount), 0) FROM Song s")
    Long getTotalPlays();

    @Query("SELECT s FROM Song s " +
            "LEFT JOIN FETCH s.artist " +
            "LEFT JOIN FETCH s.genre " +
            "ORDER BY s.playCount DESC LIMIT 5")
    List<Song> findTop5ByOrderByPlayCountDesc();
    
    @Modifying
    @Query("UPDATE Song s SET s.playCount = s.playCount + 1 WHERE s.id = :songId")
    void incrementPlayCount(@Param("songId") UUID songId);


@Modifying
    @Query("UPDATE Song s SET s.playCount = s.playCount + :count WHERE s.id = :songId")
    int incrementPlayCountBy(@Param("songId") UUID songId, @Param("count") int count);

}
