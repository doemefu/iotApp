package ch.furchert.iotapp.service;

import ch.furchert.iotapp.repository.EmailTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TokenCleanupService {

    @Autowired
    private EmailTokenRepository tokenRepository;

    @Scheduled(cron = "0 0 0 * * ?")  // Runs every day at midnight
    public void manageExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();

        // Delete tokens that expired more than 24 hours ago
        LocalDateTime expiryThreshold = now.minusHours(24);
        tokenRepository.deleteAllByExpiryDateBefore(expiryThreshold);
    }
}

