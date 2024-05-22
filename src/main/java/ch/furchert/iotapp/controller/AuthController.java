package ch.furchert.iotapp.controller;

import ch.furchert.iotapp.exception.TokenRefreshException;
import ch.furchert.iotapp.model.*;
import ch.furchert.iotapp.repository.RoleRepository;
import ch.furchert.iotapp.repository.UserRepository;
import ch.furchert.iotapp.repository.UserStatusRepository;
import ch.furchert.iotapp.security.jwt.JwtUtils;
import ch.furchert.iotapp.service.EmailServiceImpl;
import ch.furchert.iotapp.service.EmailTokenService;
import ch.furchert.iotapp.service.RefreshTokenService;
import ch.furchert.iotapp.service.UserDetailsImpl;
import ch.furchert.iotapp.util.payload.request.LoginRequest;
import ch.furchert.iotapp.util.payload.request.RegisterRequest;
import ch.furchert.iotapp.util.payload.request.VerifyRequest;
import ch.furchert.iotapp.util.payload.response.MessageResponse;
import ch.furchert.iotapp.util.payload.response.UserInfoResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@CrossOrigin(origins = "https://furchert.ch", allowCredentials = "true", maxAge = 3600)
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
    private EmailTokenService emailTokenService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Optional<User> userOptional = userRepository.findByUsername(loginRequest.getUsername());

        // If user not found by username, try by email
        if (userOptional.isEmpty()) {
            userOptional = userRepository.findByEmail(loginRequest.getUsername());
        }

        if (userOptional.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("User not found"));
        }

        User user = userOptional.get();

        if (user.getUserStatus().getName().equals(EUserStatus.UNVERIFIED)) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Verify your email first"));
        }

        // Authenticate
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), loginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        user.setLastLoginNow();
        userRepository.save(user);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);
        String jwt = jwtUtils.generateJwtToken(userDetails);

        List<String> roles = userDetails
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

        ResponseCookie jwtRefreshCookie = jwtUtils.generateRefreshJwtCookie(refreshToken.getToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .header(HttpHeaders.SET_COOKIE, jwtRefreshCookie.toString())
                .body(
                        new UserInfoResponse(
                                //new JwtResponse(
                                //jwt,
                                //refreshToken.getToken(),
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
                    .body(new MessageResponse("Error: Username is already taken!"));
        }
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT) //aka 409
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        //ups hehe System.out.println("RegisterRequest: " + registerRequest.getPassword());

        //Create new user's account
        User user = new User(
                registerRequest.getUsername(),
                registerRequest.getEmail(),
                encoder.encode(registerRequest.getPassword())
        );

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
        emailTokenService.createEmailTokenForUser(user, token);

        emailService.sendSimpleMessage(
                user.getEmail(),
                "Verify email",
                "Hello, " + user.getUsername() + " \n To verify your email, click the link below:\n " +
                        //"https://localhost:3000/auth/verifyEmail?token=" + token
                        "https://furchert.ch/auth/verifyEmail?token=" + token
        );

        return ResponseEntity
                .ok()
                .body(new MessageResponse("User registered successfully!"));
    }


    @PostMapping("/verifyEmail")
    public ResponseEntity<?> verifyEmail(@Valid @RequestBody VerifyRequest verifyRequest) {
        String token = verifyRequest.getToken();
        System.out.println("verifyEmail: " + token);

        User user;
        Optional<User> optionalUser = emailTokenService.validateEmailToken(token);

        if (optionalUser.isPresent()) {
            user = optionalUser.get();
            System.out.println("from user: " + user);

            UserStatus userStatus = userStatusRepository.findByName(EUserStatus.ACTIVE)
                    .orElseThrow(() -> new RuntimeException("Error: Status is not found."));
            user.setUserStatus(userStatus);

            userRepository.save(user);
            return ResponseEntity.ok(new MessageResponse("Email successfully verified"));
        } else {
            System.out.println("unable to map user or invalid/expired token");
            return ResponseEntity.badRequest().body(new MessageResponse("Invalid or expired token"));
        }
    }

    @PostMapping("/logout")
    //public ResponseEntity<?> logoutUser(@RequestHeader("Authorization") String authorizationHeader){
    public ResponseEntity<?> logoutUser(HttpServletRequest request) {
        String someHeaders = String.valueOf(request.getHeaderNames());

        //System.out.println("logoutUser" + authorizationHeader);
        System.out.println("logoutUser" + someHeaders);

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = userDetails.getId();
        refreshTokenService.deleteByUserId(userId);
        ResponseCookie emptyJwtCookie = jwtUtils.getCleanJwtCookie();
        ResponseCookie emptyJwtRefreshCookie = jwtUtils.getCleanJwtRefreshCookie();
        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, emptyJwtCookie.toString())
                .header(HttpHeaders.SET_COOKIE, emptyJwtRefreshCookie.toString())
                .body(new MessageResponse("You've been signed out!"));
    }

    @PostMapping("/refreshtoken")
    //public ResponseEntity<?> refreshtoken(@Valid @RequestBody TokenRefreshRequest request) {
    public ResponseEntity<?> refreshtoken(HttpServletRequest request) {
        String requestRefreshToken = jwtUtils.getJwtRefreshFromCookies(request);
        //String requestRefreshToken = request.getRefreshToken();

        if ((requestRefreshToken != null) && (!requestRefreshToken.isEmpty())) {
            return refreshTokenService.findByToken(requestRefreshToken)
                    .map(refreshTokenService::verifyExpiration)
                    .map(RefreshToken::getUser)
                    .map(user -> {
                        ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(user);
                        //String newToken = jwtUtils.generateTokenFromUsername(user.getUsername());

                        //Use the old refresh token to issue a new one
                        RefreshToken refreshToken = refreshTokenService.useToken(requestRefreshToken);
                        ResponseCookie jwtRefreshCookie = jwtUtils.replaceRefreshJwtCookie(refreshToken);

                        return ResponseEntity
                                .ok()
                                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                                .header(HttpHeaders.SET_COOKIE, jwtRefreshCookie.toString())
                                .body(
                                        //new TokenRefreshResponse(newToken, requestRefreshToken)
                                        new MessageResponse("Token is refreshed successfully!")
                                );
                    })
                    .orElseThrow(() -> new TokenRefreshException(requestRefreshToken,
                            "Refresh token is not in database!"));
        }
        return ResponseEntity.badRequest().body(new MessageResponse("Refresh Token is empty!"));
    }
}
