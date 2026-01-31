package iuh.fit.se.tramcamxuc.modules.advertisement.repository;

import iuh.fit.se.tramcamxuc.modules.advertisement.entity.Advertisement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface AdvertisementRepository extends JpaRepository<Advertisement, UUID> {
    @Query(value = "SELECT * FROM advertisements WHERE active = true ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Optional<Advertisement> findRandomActiveAd();
}
