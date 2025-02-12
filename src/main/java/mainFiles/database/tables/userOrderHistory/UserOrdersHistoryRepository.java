package mainFiles.database.tables.userOrderHistory;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("userOrdersHistoryRepository")
public interface UserOrdersHistoryRepository extends CrudRepository<UserOrderHistory, Integer> {
    List<UserOrderHistory> findByChatId(Long chatId);
    List<UserOrderHistory> findByStatus(Integer status);
}
