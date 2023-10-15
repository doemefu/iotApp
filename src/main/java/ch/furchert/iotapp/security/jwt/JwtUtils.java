package ch.furchert.iotapp.security.jwt;

import java.security.Key;
import java.util.Date;

import ch.furchert.iotapp.model.User;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import ch.furchert.iotapp.service.UserDetailsImpl;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.web.util.WebUtils;


@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${furchert.iotapp.jwtSecret}")
    private String jwtSecret;

    @Value("${furchert.iotapp.jwtExpirationMs}")
    private int jwtExpirationMs;

    @Value("${furchert.iotapp.jwtCookieName}")
    private String jwtCookie;

    @Value("${furchert.iotapp.jwtRefreshCookieName}")
    private String jwtRefreshCookie;

    public ResponseCookie generateJwtCookie(UserDetailsImpl userPrincipal) {
        String jwt = generateTokenFromUsername(userPrincipal.getUsername());
        return generateCookie(jwtCookie, jwt, "/api");
    }

    public ResponseCookie generateJwtCookie(User user){
        String jwt = generateTokenFromUsername(user.getUsername());
        return generateCookie(jwtCookie, jwt, "/api");
    }

    public ResponseCookie generateRefreshJwtCookie(String refreshToken){
        return generateCookie(jwtRefreshCookie, refreshToken, "/api/auth");
    }

    public String getJwtFromCookies(HttpServletRequest request){
        return getCookieValueByName(request, jwtCookie);
    }

    public String getJwtRefreshFromCookies(HttpServletRequest request){
        return getCookieValueByName(request, jwtRefreshCookie);
    }

    public ResponseCookie getCleanJwtCookie(){
        return ResponseCookie.from(jwtCookie).path("/api").build();
    }

    public ResponseCookie getCleanJwtRefreshCookie(){
        return ResponseCookie.from(jwtRefreshCookie).path("/api/auth/refreshtoken").build();
    }

    public String getUserNameFromJwtToken(String token){
        return Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(token).getBody().getSubject();
    }

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }
    public boolean validateJwtToken(String authToken){
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parse(authToken);
            return true;
        }catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        }catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        }catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        }catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    public String generateTokenFromUsername(String username){
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime() + jwtExpirationMs))
                .signWith(key(), SignatureAlgorithm.HS512)
                .compact();
    }

    private ResponseCookie generateCookie(String name, String value, String path){
        return ResponseCookie
                .from(name, value)
                .path(path)
                .maxAge(24*60*60)
                .httpOnly(true)
                .secure(true)
                .build();
    }

    private String getCookieValueByName(HttpServletRequest request, String name){
        Cookie cookie = WebUtils.getCookie(request, name);
        if (cookie != null){
            System.out.println("cookie: " + cookie.toString());
            return cookie.getValue();
        } else {
            System.out.println("cookie is null");
            return null;
        }
    }
}
