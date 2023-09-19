package ch.furchert.iotapp.controller;
/*
import ch.furchert.iotapp.model.User;
import ch.furchert.iotapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/register")
@CrossOrigin(origins = "http://localhost:3000")
public class RegistrationController {

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<String> registerUser(@RequestBody User user) {
        boolean isRegistered = userService.register(user);
        URI location = URI.create("http://localhost:3000/home");
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setLocation(location);

        System.out.println("Register-Daten erhalten:");
        System.out.println("Benutzername: " + user.getUsername());
        System.out.println("Passwort: " + user.getPassword());

        if (isRegistered) {
            return new ResponseEntity<>("Registration successful", responseHeaders, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Registration failed", responseHeaders, HttpStatus.BAD_REQUEST);
        }
    }
}
*/