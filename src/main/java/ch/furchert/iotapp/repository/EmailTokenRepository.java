package ch.furchert.iotapp.repository;

import ch.furchert.iotapp.model.EmailToken;
import ch.furchert.iotapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EmailTokenRepository extends JpaRepository<EmailToken, Long> {
    Optional<EmailToken> findByToken(String token);

    @Query("SELECT t FROM EmailToken t WHERE t.user = :user")
    List<EmailToken> findAllByUser(@Param("user") User user);

    void deleteAllByExpiryDateBefore(LocalDateTime expiryThreshold);
}
