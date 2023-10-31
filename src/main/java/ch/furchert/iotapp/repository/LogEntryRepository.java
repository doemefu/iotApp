package ch.furchert.iotapp.repository;

import ch.furchert.iotapp.model.LogEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogEntryRepository extends JpaRepository<LogEntry, Long> {

}
