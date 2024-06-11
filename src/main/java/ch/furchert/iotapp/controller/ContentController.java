package ch.furchert.iotapp.controller;

import ch.furchert.iotapp.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/get")
public class ContentController {
    private static final Logger logger = LoggerFactory.getLogger(ContentController.class);

    @Autowired
    UserRepository userRepository;

    @GetMapping("/all")
    public String allAccess() {

        logger.info("get/all request received");
        return "This is public Content.";
    }

    @GetMapping("/user")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public String userAccess() {
        return "Content that is only for logged in users.";
    }

    @GetMapping("/mod")
    @PreAuthorize("hasRole('MODERATOR')")
    public String moderatorAccess() {
        return "Content that is only for moderators";
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminAccess() {
        return "Content that is only for admins";
    }

}