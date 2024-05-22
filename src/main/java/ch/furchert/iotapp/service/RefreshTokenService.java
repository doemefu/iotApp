package ch.furchert.iotapp.service;

import ch.furchert.iotapp.exception.TokenRefreshException;
import ch.furchert.iotapp.model.RefreshToken;
import ch.furchert.iotapp.repository.RefreshTokenRepository;
import ch.furchert.iotapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {
    @Autowired
    UserRepository userRepository;
    @Value("${furchert.iotapp.jwtRefreshExpirationMs}")
    private Long refreshTokenDurationMs;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken createRefreshToken(Long userId) {
        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setUser(userRepository.findById(userId).get());
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setToken(UUID.randomUUID().toString());

        refreshTokenRepository.save(refreshToken);
        return refreshToken;
    }

    //Vlt in JwtUtils verschieben
    public RefreshToken useToken(String oldTokenString) {

        Optional<RefreshToken> oldTokenOpt = refreshTokenRepository.findByToken(oldTokenString);
        RefreshToken oldToken = oldTokenOpt.get();
        RefreshToken newToken = new RefreshToken();

        newToken.setUser(oldToken.getUser());
        newToken.setExpiryDate(oldToken.getExpiryDate());
        newToken.setToken(UUID.randomUUID().toString());

        refreshTokenRepository.save(newToken);
        refreshTokenRepository.delete(oldToken);

        return newToken;
    }


    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException(
                    token.getToken(),
                    "Refresh token is expired. Please make a new Sign-In request"
            );
        }

        return token;
    }

    @Transactional
    public int deleteByUserId(Long userId) {
        return refreshTokenRepository.deleteByUser(userRepository.findById(userId).get());
    }

    @Transactional
    public int deleteByToken(RefreshToken token) {
        return refreshTokenRepository.deleteByToken(token.getToken());
    }

}
