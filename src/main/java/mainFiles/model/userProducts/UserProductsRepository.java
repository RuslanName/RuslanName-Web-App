package mainFiles.model.userProducts;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository("userProductsRepository")
public interface UserProductsRepository extends CrudRepository<UserProduct, Integer> {
    UserProduct findByChatIdAndProductId(long chatId, int productId);
}
