package mainFiles.database.tables.userOrder;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository("userOrdersRepository")
public interface UserOrdersRepository extends CrudRepository<UserOrder, Integer> {
}
