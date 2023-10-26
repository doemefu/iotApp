package ch.furchert.iotapp.controller;

import ch.furchert.iotapp.exception.TokenRefreshException;
import ch.furchert.iotapp.model.*;
import ch.furchert.iotapp.repository.RoleRepository;
import ch.furchert.iotapp.repository.UserRepository;
import ch.furchert.iotapp.repository.UserStatusRepository;
import ch.furchert.iotapp.security.jwt.JwtUtils;
import ch.furchert.iotapp.service.EmailServiceImpl;
import ch.furchert.iotapp.service.EmailVerificationTokenService;
import ch.furchert.iotapp.service.RefreshTokenService;
import ch.furchert.iotapp.service.UserDetailsImpl;
import ch.furchert.iotapp.util.payload.request.LoginRequest;
import ch.furchert.iotapp.util.payload.request.RegisterRequest;
import ch.furchert.iotapp.util.payload.request.TokenRefreshRequest;
import ch.furchert.iotapp.util.payload.request.VerifyRequest;
import ch.furchert.iotapp.util.payload.response.JwtResponse;
import ch.furchert.iotapp.util.payload.response.MessageResponse;
import ch.furchert.iotapp.util.payload.response.TokenRefreshResponse;
import ch.furchert.iotapp.util.payload.response.UserInfoResponse;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("api/auth")
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    UserStatusRepository userStatusRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    RefreshTokenService refreshTokenService;

    @Autowired
    EmailServiceImpl emailService;

    @Autowired
    private EmailVerificationTokenService emailVerificationTokenService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        //ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);
        String jwt = jwtUtils.generateJwtToken(userDetails);

        List<String> roles = userDetails
                                .getAuthorities()
                                .stream()
                                .map(GrantedAuthority::getAuthority)
                                .collect(Collectors.toList());

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

        //ResponseCookie jwtRefreshCookie = jwtUtils.generateRefreshJwtCookie(refreshToken.getAccessToken());

        return ResponseEntity.ok()
                //.header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                //.header(HttpHeaders.SET_COOKIE, jwtRefreshCookie.toString())
                .body(
                    //new UserInfoResponse(
                    new JwtResponse(
                            jwt,
                            refreshToken.getToken(),
                            userDetails.getId(),
                            userDetails.getUsername(),
                            userDetails.getEmail(),
                            roles
                    )
                );
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT) //aka 409
                    .header("ResponseMessage", "Error: Username is already taken!")
                    .build();
        }
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT) //aka 409
                    .header("ResponseMessage", "Error: Email is already in use!")
                    .build();
        }

        //ups hehe System.out.println("RegisterRequest: " + registerRequest.getPassword());

        //Create new user's account
        User user = new User(
                registerRequest.getUsername(),
                registerRequest.getEmail(),
                encoder.encode(registerRequest.getPassword())
        );

        //TODO: Include some kind of verification process
        UserStatus userStatus = userStatusRepository.findByName(EUserStatus.UNVERIFIED)
                .orElseThrow(() -> new RuntimeException("Error: Status is not found."));
        user.setUserStatus(userStatus);

        Set<String> strRoles = registerRequest.getRoles();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(adminRole);
                        break;
                    case "mod":
                        Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(modRole);
                        break;
                    default:
                        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(userRole);
                }
            });
        }
        user.setRoles(roles);
        userRepository.save(user);

        String token = UUID.randomUUID().toString();

        // Save the token and user's email in the database
        emailVerificationTokenService.createEmailVerificationTokenForUser(user, token);

        emailService.sendSimpleMessage(user.getEmail(), "To verify your email, click the link below:\n" +
                "https://localhost:3000/verifyEmail?token=" + token, "Hello, " + user.getUsername() + " please verify your email address here: <link>");

        return ResponseEntity
                .ok()
                .header("ResponseMessage", "User registered successfully!")
                .build();
    }


    @PostMapping("/verifyEmail")
    public ResponseEntity<?> verifyEmail(@Valid @RequestBody VerifyRequest verifyRequest) {
        String token = verifyRequest.getToken();
        System.out.println("verifyEmail: " + token);
        User user = emailVerificationTokenService.validateEmailVerificationToken(token);
        if (user != null) {
            System.out.println("from user: " + user.toString());
            UserStatus userStatus = userStatusRepository.findByName(EUserStatus.ACTIVE)
                    .orElseThrow(() -> new RuntimeException("Error: Status is not found."));
            user.setUserStatus(userStatus);

            emailVerificationTokenService.deleteTokenByValue(token);

            userRepository.save(user);
            return ResponseEntity.ok(new MessageResponse("Email verified successfully"));
        } else {
            System.out.println("unable to map user or invalid/expired token");
            return ResponseEntity.badRequest().body(new MessageResponse("Invalid or expired token"));
        }
    }
/*
    @PostMapping("/verifyEmail")
    public ResponseEntity<?> verifyEmail(@Valid @RequestBody VerifyRequest verifyRequest) {
        String token = verifyRequest.getToken();
        System.out.println("Verifying Email" + token);
        return ResponseEntity.ok(new MessageResponse("Email verified successfully"));

    }

 */
/*
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(HttpServletRequest request) {
        System.out.println("request: " + request.toString());

        String refreshToken = jwtUtils.getJwtRefreshFromCookies(request);

        System.out.println("refreshToken: " + refreshToken);

        if ((refreshToken != null) && (!refreshToken.isEmpty())) {
            refreshTokenService.findByToken(refreshToken)
                    .map(refreshTokenService::deleteByToken)
                    .orElseThrow(() -> new TokenRefreshException(refreshToken,
                            "Refresh token is not in database!"));
        }

        ResponseCookie jwtCookie = jwtUtils.getCleanJwtCookie();
        ResponseCookie jwtRefreshCookie = jwtUtils.getCleanJwtRefreshCookie();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .header(HttpHeaders.SET_COOKIE, jwtRefreshCookie.toString())
                .body(new MessageResponse("You've been signed out!"));
    }
*/

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(){
        System.out.println("logoutUser");
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = userDetails.getId();
        refreshTokenService.deleteByUserId(userId);
        return ResponseEntity
                .ok()
                .body(new MessageResponse("You've been signed out!"));
    }

    @PostMapping("/refreshtoken")
    public ResponseEntity<?> refreshtoken(@Valid @RequestBody TokenRefreshRequest request) {
        //String refreshToken = jwtUtils.getJwtRefreshFromCookies(request);
        String requestRefreshToken = request.getRefreshToken();

        if ((requestRefreshToken != null) && (!requestRefreshToken.isEmpty())) {
            return refreshTokenService.findByToken(requestRefreshToken)
                    .map(refreshTokenService::verifyExpiration)
                    .map(RefreshToken::getUser)
                    .map(user -> {
                        //ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(user);
                        String newToken = jwtUtils.generateTokenFromUsername(user.getUsername());
                        return ResponseEntity
                                .ok()
                                //.header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                                .body(
                                        //TODO: issue new refresh token
                                        new TokenRefreshResponse(newToken, requestRefreshToken)
                                        //new MessageResponse("Token is refreshed successfully!")
                        );
                    })
                    .orElseThrow(() -> new TokenRefreshException(requestRefreshToken,
                            "Refresh token is not in database!"));
        }
        return ResponseEntity.badRequest().body(new MessageResponse("Refresh Token is empty!"));
    }
}
