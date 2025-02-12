package mainFiles.database.tables.userCart;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository("userProductsRepository")
public interface UserCartsRepository extends CrudRepository<UserCart, Integer> {
    List<UserCart> findByChatId(Long chatId);
    List<UserCart> findByProductIdAndChatId(Integer productId, Long chatId);
    List<UserCart> findByProductIdAndQuantityGreaterThan(Integer productId, Integer quantity);
    List<UserCart> findByChatIdAndSelected(Long chatId, boolean selected);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserCart u WHERE u.chatId = :chatId AND u.selected = true")
    void deleteSelectedByChatId(Long chatId);
}

