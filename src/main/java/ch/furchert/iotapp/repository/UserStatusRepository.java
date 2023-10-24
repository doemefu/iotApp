package ch.furchert.iotapp.repository;

import ch.furchert.iotapp.model.EUserStatus;
import ch.furchert.iotapp.model.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserStatusRepository extends JpaRepository<UserStatus, Long> {
    Optional<UserStatus> findByName(EUserStatus name);
}
