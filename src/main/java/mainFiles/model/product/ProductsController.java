package mainFiles.model.product;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductsController {
    @Autowired
    private ProductsRepository productsRepository;

    @GetMapping
    public List<Product> getAllProducts() {
        return (List<Product>) productsRepository.findAll();
    }
}

