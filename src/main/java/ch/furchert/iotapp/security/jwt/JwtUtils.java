package ch.furchert.iotapp.security.jwt;

import ch.furchert.iotapp.model.RefreshToken;
import ch.furchert.iotapp.model.User;
import ch.furchert.iotapp.service.UserDetailsImpl;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import javax.crypto.SecretKey;
import java.util.Date;


@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${furchert.iotapp.jwtSecret}")
    private String jwtSecret;

    @Value("${furchert.iotapp.jwtExpirationMs}")
    private int jwtExpirationMs;

    @Value("${furchert.iotapp.jwtCookieName}")
    private String jwtCookieName;

    @Value("${furchert.iotapp.jwtRefreshCookieName}")
    private String jwtRefreshCookieName;

    public ResponseCookie generateJwtCookie(UserDetailsImpl userPrincipal) {
        String jwt = generateTokenFromUsername(userPrincipal.getUsername());
        return generateCookie(jwtCookieName, jwt, "/api");
    }

    public ResponseCookie generateJwtCookie(User user) {
        String jwt = generateTokenFromUsername(user.getUsername());
        return generateCookie(jwtCookieName, jwt, "/api");
    }

    public ResponseCookie generateRefreshJwtCookie(String refreshToken) {
        return generateCookie(jwtRefreshCookieName, refreshToken, "/api/auth");
    }

    public ResponseCookie replaceRefreshJwtCookie(RefreshToken refreshToken) {
        return generateCookie(jwtRefreshCookieName, refreshToken.getToken(), "/api/auth");
    }

    public String getJwtFromCookies(HttpServletRequest request) {
        return getCookieValueByName(request, jwtCookieName);
    }

    public String getJwtRefreshFromCookies(HttpServletRequest request) {
        return getCookieValueByName(request, jwtRefreshCookieName);
    }

    public ResponseCookie getCleanJwtCookie() {
        return ResponseCookie.from(jwtCookieName).path("/api").build();
    }

    public ResponseCookie getCleanJwtRefreshCookie() {
        return ResponseCookie.from(jwtRefreshCookieName).path("/api/auth/refreshtoken").build();
    }

    private ResponseCookie generateCookie(String name, String value, String path) {
        return ResponseCookie
                .from(name, value)
                .path(path)
                .maxAge(24 * 60 * 60)
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .build();
    }

    private String getCookieValueByName(HttpServletRequest request, String name) {
        Cookie cookie = WebUtils.getCookie(request, name);
        if (cookie != null) {
            System.out.println("cookie: " + cookie);
            return cookie.getValue();
        } else {
            System.out.println("cookie is null");
            return null;
        }
    }

    public String generateJwtToken(UserDetailsImpl userPrincipal) {
        return generateTokenFromUsername(userPrincipal.getUsername());
    }

    public String generateTokenFromUsername(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(new Date().getTime() + jwtExpirationMs))
                .signWith(key())
                .compact();
    }

    public String getUserNameFromJwtToken(String token) {
        Jws<Claims> claimsJws = Jwts.parser()
                .verifyWith(key())  // Use Key for verification
                .build()
                .parseSignedClaims(token);
        return claimsJws.getPayload().getSubject();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts
                    .parser()
                    .verifyWith(key())
                    .build()
                    .parseSignedClaims(authToken);
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    private SecretKey key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }
}
