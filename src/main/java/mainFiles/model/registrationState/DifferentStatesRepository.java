package mainFiles.model.registrationState;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository("differentStatesRepository")
public interface DifferentStatesRepository extends CrudRepository<DifferentState, Long> {
}
