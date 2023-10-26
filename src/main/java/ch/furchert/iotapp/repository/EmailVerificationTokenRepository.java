package ch.furchert.iotapp.repository;

import ch.furchert.iotapp.model.EmailVerificationToken;
import ch.furchert.iotapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {
    Optional<EmailVerificationToken> findByToken(String token);

    @Query("SELECT t FROM EmailVerificationToken t WHERE t.user = :user")
    List<EmailVerificationToken> findAllByUser(@Param("user") User user);

}
