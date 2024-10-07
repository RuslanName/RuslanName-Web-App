package mainFiles.model.registrationState;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegistrationStateRepository extends CrudRepository<RegistrationState, Long> {
}
