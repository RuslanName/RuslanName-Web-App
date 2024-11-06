package mainFiles.model.product;

import mainFiles.model.userCarts.UserCartsRepository;
import mainFiles.model.userCarts.UserCart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products_data")
@CrossOrigin(origins = "https://magazin-ruslanname.amvera.io")
public class ProductsController {

    @Autowired
    private ProductsRepository productsRepository;

    @Autowired
    private UserCartsRepository userCartsRepository;

    @GetMapping
    public List<Product> getAllProducts() {
        return (List<Product>) productsRepository.findAll();
    }

    @GetMapping("/user/{chatId}")
    public List<Product> getProductsForUser(@PathVariable Long chatId) {
        List<UserCart> userCartItems = userCartsRepository.findByChatId(chatId);

        return (List<Product>) productsRepository.findAllById(
                userCartItems.stream()
                        .map(UserCart::getProductId)
                        .toList()
        );
    }
}


