package iuh.fit.se.tramcamxuc.modules.auth.repository;

import iuh.fit.se.tramcamxuc.modules.auth.entity.RefreshToken;
import iuh.fit.se.tramcamxuc.modules.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByUser(User user);
}