package iuh.fit.se.tramcamxuc.modules.genre.repository;

import iuh.fit.se.tramcamxuc.modules.genre.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface GenreRepository extends JpaRepository<Genre, UUID> {
}