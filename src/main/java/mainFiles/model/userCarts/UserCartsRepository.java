package mainFiles.model.userCarts;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("userProductsRepository")
public interface UserCartsRepository extends CrudRepository<UserCart, Integer> {
    UserCart findByChatIdAndProductId(long chatId, int productId);

    List<UserCart> findByChatId(Long chatId);
}

