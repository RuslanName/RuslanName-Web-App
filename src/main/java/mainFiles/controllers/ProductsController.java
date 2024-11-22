package mainFiles.controllers;

import mainFiles.database.tables.product.Product;
import mainFiles.database.tables.product.ProductsRepository;
import mainFiles.database.tables.userCarts.UserCartsRepository;
import mainFiles.database.tables.userCarts.UserCart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products_data")
@CrossOrigin
public class ProductsController {

    @Value("${web.url}")
    private String URL;

    @Autowired
    private ProductsRepository productsRepository;

    @Autowired
    private UserCartsRepository userCartsRepository;

    @GetMapping
    @CrossOrigin(origins = "${web.url}")
    public List<Product> getAllProducts() {
        return (List<Product>) productsRepository.findAll();
    }

    @GetMapping("/user/{chatId}")
    @CrossOrigin(origins = "${web.url}")
    public List<Product> getProductsForUser(@PathVariable Long chatId) {
        List<UserCart> userCartItems = userCartsRepository.findByChatId(chatId);

        return (List<Product>) productsRepository.findAllById(
                userCartItems.stream()
                        .map(UserCart::getProductId)
                        .collect(Collectors.toList())
        );
    }
}
