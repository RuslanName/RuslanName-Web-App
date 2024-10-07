package mainFiles.model.userProducts;

import mainFiles.model.user.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository("userProductsRepository")
public interface UserProductsRepository extends CrudRepository<UserProducts, Integer> {
    UserProducts findByChatIdAndProductId(long chatId, int productId);
}
