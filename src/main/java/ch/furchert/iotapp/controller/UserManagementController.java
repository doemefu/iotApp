package ch.furchert.iotapp.controller;

import ch.furchert.iotapp.model.User;
import ch.furchert.iotapp.util.payload.response.MessageResponse;
import ch.furchert.iotapp.repository.UserRepository;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

//TODO: Forgot password
//TODO: Admin userhandling
//TODO: Delete account
//TODO: User status handling

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("api/user-management")
public class UserManagementController {

    @Autowired
    UserRepository userRepository;

    //TODO: would it make more sense to send only few attributes in the get/allUsers method
    // and show the rest here?
    //Pro: less traffic because you dont send all attributes
    //Contra: more requests and you cant sort or filter by attributes
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

            // Save the updated user
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

    @GetMapping("/forgotPassword")
    public ResponseEntity<?> forgotPassword() {
        return ResponseEntity
                .ok()
                .body(new MessageResponse("Forgot password"));
    }

    //TODO: Admin can show all users
    @GetMapping("/allUsers")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

}
