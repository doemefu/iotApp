package ch.furchert.iotapp.service;

import ch.furchert.iotapp.model.EmailVerificationToken;
import ch.furchert.iotapp.model.User;
import ch.furchert.iotapp.repository.EmailVerificationTokenRepository;
import ch.furchert.iotapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class EmailVerificationTokenService {

    @Autowired
    private EmailVerificationTokenRepository tokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    public void createEmailVerificationTokenForUser(User user, String token) {
        EmailVerificationToken myToken = new EmailVerificationToken();
        myToken.setToken(encoder.encode(token));
        myToken.setExpiryDate(LocalDateTime.now().plusHours(24)); // Token expires after 24 hours
        myToken.setUser(user);
        tokenRepository.save(myToken);
    }
/*
    public User validateEmailVerificationToken(String token) {
        Optional<EmailVerificationToken> optToken = tokenRepository.findByToken(encoder.encode(token));
        if (!optToken.isPresent()) {
            System.out.println("token not found");
            return null;
        }
        EmailVerificationToken myToken = optToken.get();
        if (myToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            System.out.println("token expired");
            return null;
        }
        return myToken.getUser();
    }
*/

    public User validateEmailVerificationToken(String token) {
        List<EmailVerificationToken> storedTokens = tokenRepository.findAll(); // Fetch all tokens

        for (EmailVerificationToken storedToken : storedTokens) {
            if (encoder.matches(token, storedToken.getToken())) {
                if (storedToken.getExpiryDate().isBefore(LocalDateTime.now())) {
                    System.out.println("token expired");
                    return null;
                }
                return storedToken.getUser();
            }
        }

        System.out.println("token not found");
        return null;
    }

    public void deleteToken(EmailVerificationToken token) {
        tokenRepository.delete(token);
    }

    public void deleteTokenByValue(String tokenValue) {
        Optional<EmailVerificationToken> optToken = tokenRepository.findByToken(tokenValue);
        optToken.ifPresent(tokenRepository::delete);
    }

}
