package mainFiles.model.product;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("productsRepository")
public interface ProductsRepository extends CrudRepository<Product, Integer> {
    @Query("SELECT p FROM ProductsData p WHERE p.visibility = true")
    List<Product> findVisibleProducts();
}