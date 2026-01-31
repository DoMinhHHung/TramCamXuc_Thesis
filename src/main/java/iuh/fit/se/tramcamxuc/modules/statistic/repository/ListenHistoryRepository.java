package iuh.fit.se.tramcamxuc.modules.statistic.repository;

import iuh.fit.se.tramcamxuc.modules.statistic.entity.ListenHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ListenHistoryRepository extends JpaRepository<ListenHistory, UUID> {

    @Query(value = "SELECT CAST(listened_at AS DATE) as date, COUNT(*) as count " +
            "FROM listen_history " +
            "WHERE listened_at >= :startDate " +
            "GROUP BY CAST(listened_at AS DATE) " +
            "ORDER BY date ASC",
            nativeQuery = true)
    List<Object[]> getStatsByDate(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT lh.song, COUNT(lh) as playCount " +
            "FROM ListenHistory lh " +
            "WHERE lh.listenedAt >= :startDate " +
            "GROUP BY lh.song " +
            "ORDER BY COUNT(lh) DESC")
    List<Object[]> getTopPlayedSongs(@Param("startDate") LocalDateTime startDate, Pageable pageable);

    @Query("SELECT COUNT(DISTINCT lh.user.id) FROM ListenHistory lh WHERE lh.song.id = :songId")
    Long countUniqueListeners(@Param("songId") UUID songId);
}