package mainFiles.tables.userCarts;

import mainFiles.service.UserCart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user_carts_data")
@CrossOrigin
public class UserCartController {

    @Value("${web.url}")
    private String webURL;

    @Autowired
    private UserCart userCart;

    @PostMapping("/update")
    @CrossOrigin(origins = "${web.url}")
    public String addProductToCart(
            @RequestParam long chatId,
            @RequestParam int productId,
            @RequestParam int quantity) {

        userCart.updateProductQuantity(chatId, productId, quantity);
        return "Product added to cart successfully!";
    }

    @PostMapping("/update_selection")
    @CrossOrigin(origins = "${web.url}")
    public String updateProductSelection(
            @RequestParam long chatId,
            @RequestParam int productId,
            @RequestParam boolean selected) {

        userCart.updateProductSelection(chatId, productId, selected);
        return "Product selection updated successfully!";
    }

    @GetMapping("/load")
    @CrossOrigin(origins = "${web.url}")
    public List<mainFiles.tables.userCarts.UserCart> getUserCart(@RequestParam long chatId) {
        return userCart.getUserCartByChatId(chatId);
    }
}