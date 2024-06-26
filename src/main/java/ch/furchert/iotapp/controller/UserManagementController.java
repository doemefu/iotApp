package ch.furchert.iotapp.controller;

import ch.furchert.iotapp.model.User;
import ch.furchert.iotapp.repository.UserRepository;
import ch.furchert.iotapp.service.EmailServiceImpl;
import ch.furchert.iotapp.service.EmailTokenService;
import ch.furchert.iotapp.service.UserDetailsImpl;
import ch.furchert.iotapp.util.payload.request.ForgotPasswordRequest;
import ch.furchert.iotapp.util.payload.request.ResetPasswordRequest;
import ch.furchert.iotapp.util.payload.response.MessageResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

//TODO: Admin userhandling
//TODO: Delete account
//TODO: User status handling

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("api/user-management")
public class UserManagementController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    EmailServiceImpl emailService;
    @Autowired
    PasswordEncoder encoder;
    @Autowired
    private EmailTokenService emailTokenService;

    @GetMapping("/showUser/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isPresent()) {
            return new ResponseEntity<>(userOptional.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new MessageResponse("User not found"), HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/deleteUser/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isPresent()) {
            userRepository.deleteById(id);
            return ResponseEntity
                    .ok()
                    .body(new MessageResponse("User deleted successfully!"));
        } else {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("User not found!"));
        }
    }

    @PutMapping("/updateUser/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @Valid @RequestBody User updatedUser) {
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isPresent()) {
            User existingUser = userOptional.get();

            //TODO: Update fields as needed.
            existingUser.setUsername(updatedUser.getUsername());
            existingUser.setEmail(updatedUser.getEmail());
            existingUser.setRoles(updatedUser.getRoles());

            userRepository.save(existingUser);

            return new ResponseEntity<>(existingUser, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new MessageResponse("User not found"), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/showRoles")
    public ResponseEntity<?> showAllRoles() {
        return ResponseEntity
                .ok()
                .body(new MessageResponse("Show all roles"));
    }

    @PostMapping("/forgotPassword")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {

        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());

        if (userOptional.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NO_CONTENT) //aka 204: processed successfully but no content to return
                    .build();
        }

        User existingUser = userOptional.get();

        String token = UUID.randomUUID().toString();

        emailTokenService.createEmailTokenForUser(existingUser, token);

        emailService.sendSimpleMessage(
                existingUser.getEmail(),
                "Password Reset",
                "Hello, " + existingUser.getUsername() + " \n please reset your password here: https://furchert.ch/auth/resetPassword?token=" + token);

        return ResponseEntity
                .ok()
                .body(new MessageResponse("Reset successfully initiated!"));
    }

    @PostMapping("/resetPassword")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        Optional<User> userOptional;
        User user;

        if (request.getToken() != null) {
            userOptional = emailTokenService.validateEmailToken(request.getToken());

        } else {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();

            userOptional = userRepository.findByUsername(userDetails.getUsername());
        }

        if (userOptional.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(new MessageResponse("User or token not found!")
                    );
        }
        user = userOptional.get();

        if (request.getToken() == null) {
            if (!encoder.matches(request.getOldPassword(), user.getPassword())) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(new MessageResponse("Old password is incorrect!"));
            }
        }

        System.out.println("old password: " + user.getPassword());
        user.setPassword(encoder.encode(request.getNewPassword()));
        System.out.println("new password: " + user.getPassword());
        userRepository.save(user);

        return ResponseEntity
                .ok()
                .body(new MessageResponse("Password reset successfully!"));
    }


    @GetMapping("/allUsers")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }
}
