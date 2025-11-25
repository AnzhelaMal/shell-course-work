package edu.semitotal.commander.server.state;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppStateRepository extends CrudRepository<AppState, Long> {

    @Query("SELECT * FROM app_state ORDER BY last_updated DESC LIMIT 1")
    Optional<AppState> findLatest();
}
