package mainFiles.model.userCarts;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository("userProductsRepository")
public interface UserCartsRepository extends CrudRepository<UserCart, Long> {
    UserCart findByChatIdAndProductId(long chatId, int productId);
}
