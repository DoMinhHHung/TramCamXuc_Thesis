package iuh.fit.se.tramcamxuc.modules.album.repository;

import iuh.fit.se.tramcamxuc.modules.album.entity.Album;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AlbumRepository extends JpaRepository<Album, UUID> {
    boolean existsBySlug(String slug);

    @Query("SELECT a FROM Album a " +
            "LEFT JOIN FETCH a.songs s " +
            "LEFT JOIN FETCH a.artist " +
            "WHERE a.slug = :slug")
    Optional<Album> findBySlugWithSongs(@Param("slug") String slug);

    @Query("SELECT a FROM Album a JOIN FETCH a.artist WHERE a.artist.id = :artistId")
    Page<Album> findByArtistId(@Param("artistId") UUID artistId, Pageable pageable);


    @Query("SELECT a FROM Album a " +
            "LEFT JOIN FETCH a.songs s " +
            "LEFT JOIN FETCH a.artist " +
            "WHERE a.id = :id")
    Optional<Album> findByIdWithSongs(@Param("id") UUID id);

    Page<Album> findByArtistIdOrderByReleaseDateDesc(UUID artistId, Pageable pageable);

    @Query("SELECT DISTINCT a FROM Album a " +
            "JOIN FETCH a.artist ar " +
            "LEFT JOIN FETCH a.songs s " +
            "WHERE LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(ar.artistName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Album> searchAlbumsForAdmin(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT DISTINCT a FROM Album a " +
            "JOIN FETCH a.artist " +
            "LEFT JOIN FETCH a.songs")
    Page<Album> findAllWithArtist(Pageable pageable);
}