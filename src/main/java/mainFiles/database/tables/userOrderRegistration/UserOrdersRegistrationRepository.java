package mainFiles.database.tables.userOrderRegistration;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository("userOrdersRegistrationRepository")
public interface UserOrdersRegistrationRepository extends CrudRepository<UserOrderRegistration, Integer> {
}
