package ch.furchert.iotapp.service;

import ch.furchert.iotapp.model.ERole;
import ch.furchert.iotapp.model.EUserStatus;
import ch.furchert.iotapp.model.Role;
import ch.furchert.iotapp.model.UserStatus;
import ch.furchert.iotapp.repository.RoleRepository;
import ch.furchert.iotapp.repository.UserStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class StartupDataLoader implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserStatusRepository userStatusRepository;

    @Override
    public void run(String... args) throws Exception {
        loadEnumValues();
    }

    private void loadEnumValues() {
        for (ERole role : ERole.values()) {
            if (roleRepository.findByName(role).isEmpty()) {
                Role newRole = new Role();
                newRole.setName(role);
                roleRepository.save(newRole);
            }
        }

        for (EUserStatus status : EUserStatus.values()) {
            if (userStatusRepository.findByName(status).isEmpty()) {
                UserStatus newUserStatus = new UserStatus();
                newUserStatus.setName(status);
                userStatusRepository.save(newUserStatus);
            }
        }
    }
}
