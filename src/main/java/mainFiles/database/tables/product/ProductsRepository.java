package mainFiles.database.tables.product;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository("productsRepository")
public interface ProductsRepository extends CrudRepository<Product, Integer> {
}