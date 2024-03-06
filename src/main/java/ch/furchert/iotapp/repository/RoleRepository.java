package ch.furchert.iotapp.repository;

import ch.furchert.iotapp.model.ERole;
import ch.furchert.iotapp.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(ERole name);
}
