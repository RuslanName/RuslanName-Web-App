package mainFiles.database.tables.userOrderHistory;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository("userOrdersHistoryRepository")
public interface UserOrdersHistoryRepository extends CrudRepository<UserOrderHistory, Integer> {
}
