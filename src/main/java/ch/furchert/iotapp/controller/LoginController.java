package ch.furchert.iotapp.controller;

import ch.furchert.iotapp.model.User;
import ch.furchert.iotapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Optional;


@RestController
@RequestMapping("/login")
@CrossOrigin(origins = "http://localhost:3000")
public class LoginController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<String> loginUser(@RequestBody User user) {

        System.out.println("Login-Daten erhalten:");
        System.out.println("Benutzername: " + user.getUsername());
        System.out.println("Passwort: " + user.getPassword());

        URI location = URI.create("http://localhost:3000/home");
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setLocation(location);

        Optional<User> optionalUser = userRepository.findByUsername(user.getUsername());

        if (optionalUser.isPresent() && optionalUser.get().getPassword().equals(user.getPassword())) {
            return new ResponseEntity<>("Login worked", responseHeaders, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Login did not work", responseHeaders, HttpStatus.UNAUTHORIZED);
        }

    }

}
