package mainFiles.database.tables.userOrder;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("userOrdersRepository")
public interface UserOrdersRepository extends CrudRepository<UserOrder, Integer> {
    List<UserOrder> findByChatId(Long chatId);
    List<UserOrder> findByStatus(Integer status);
}
