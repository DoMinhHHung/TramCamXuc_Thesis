package iuh.fit.se.tramcamxuc.modules.advertisement.repository;

import iuh.fit.se.tramcamxuc.modules.advertisement.entity.Advertisement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AdvertisementRepository extends JpaRepository<Advertisement, UUID> {
    @Query(value = "SELECT * FROM advertisements WHERE active = true ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    Optional<Advertisement> findRandomActiveAd();
    
    @Query(value = "SELECT * FROM advertisements WHERE " +
           "(:keyword IS NULL OR title ILIKE CONCAT('%', CAST(:keyword AS text), '%') " +
           "OR sponsor_name ILIKE CONCAT('%', CAST(:keyword AS text), '%')) " +
           "AND (:active IS NULL OR active = :active) " +
           "ORDER BY created_at DESC",
           countQuery = "SELECT COUNT(*) FROM advertisements WHERE " +
           "(:keyword IS NULL OR title ILIKE CONCAT('%', CAST(:keyword AS text), '%') " +
           "OR sponsor_name ILIKE CONCAT('%', CAST(:keyword AS text), '%')) " +
           "AND (:active IS NULL OR active = :active)",
           nativeQuery = true)
    Page<Advertisement> searchAds(@Param("keyword") String keyword, 
                                   @Param("active") Boolean active, 
                                   Pageable pageable);
    
    long countByActive(boolean active);
    
    @Query("SELECT SUM(a.impressions) FROM Advertisement a")
    Long getTotalImpressions();
    
    @Query("SELECT SUM(a.clicks) FROM Advertisement a")
    Long getTotalClicks();
    
    @Modifying
    @Query("UPDATE Advertisement a SET a.impressions = a.impressions + 1 WHERE a.id = :id")
    void incrementImpressions(@Param("id") UUID id);
    
    @Modifying
    @Query("UPDATE Advertisement a SET a.clicks = a.clicks + 1 WHERE a.id = :id")
    void incrementClicks(@Param("id") UUID id);
    
    @Query(value = "SELECT DATE(created_at) as date, COUNT(*) as count " +
           "FROM advertisements " +
           "WHERE created_at >= :startDate " +
           "GROUP BY DATE(created_at) " +
           "ORDER BY date", nativeQuery = true)
    List<Object[]> getAdsByDate(@Param("startDate") LocalDateTime startDate);
}
