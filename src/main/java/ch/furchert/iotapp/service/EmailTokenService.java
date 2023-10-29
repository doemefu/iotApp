package ch.furchert.iotapp.service;

import ch.furchert.iotapp.model.EmailToken;
import ch.furchert.iotapp.model.User;
import ch.furchert.iotapp.repository.EmailTokenRepository;
import ch.furchert.iotapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class EmailTokenService {

    @Autowired
    private EmailTokenRepository tokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    public void createEmailTokenForUser(User user, String token) {
        EmailToken myToken = new EmailToken();
        myToken.setToken(encoder.encode(token));
        myToken.setExpiryDate(LocalDateTime.now().plusHours(24)); // Token expires after 24 hours
        myToken.setUser(user);
        myToken.setUsed(false);
        tokenRepository.save(myToken);
    }

    public User validateEmailToken(String token) {
        List<EmailToken> storedTokens = tokenRepository.findAll(); // Fetch all tokens

        for (EmailToken storedToken : storedTokens) {
            if (encoder.matches(token, storedToken.getToken())) {
                if (storedToken.getUsed()) {
                    System.out.println("token already used");
                    return null;
                }
                if (storedToken.getExpiryDate().isBefore(LocalDateTime.now())) {
                    System.out.println("token expired");
                    return null;
                }
                storedToken.setUsed(true);
                return storedToken.getUser();
            }
        }

        System.out.println("token not found");
        return null;
    }

    public void deleteToken(EmailToken token) {
        tokenRepository.delete(token);
    }

    public void deleteTokenByValue(String tokenValue) {
        Optional<EmailToken> optToken = tokenRepository.findByToken(tokenValue);
        optToken.ifPresent(tokenRepository::delete);
    }

}
