package mainFiles.database.tables.userOrders;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository("userOrdersRepository")
public interface UserOrdersRepository extends CrudRepository<UserOrder, Integer> {
    UserOrder findByChatIdAndProductId(long chatId, int productId);
}
