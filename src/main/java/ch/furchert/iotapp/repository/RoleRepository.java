package ch.furchert.iotapp.repository;

import java.util.Optional;

import ch.furchert.iotapp.model.Role;
import ch.furchert.iotapp.model.ERole;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(ERole name);
}
