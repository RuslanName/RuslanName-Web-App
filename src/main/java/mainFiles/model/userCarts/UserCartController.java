package mainFiles.model.userCarts;

import mainFiles.service.UserCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user_carts_data")
public class UserCartController {

    @Autowired
    private UserCartService userCartService;

    @PostMapping("/update")
    public String addProductToCart(@RequestParam long chatId, @RequestParam int productId, @RequestParam int quantity) {
        userCartService.updateProductQuantity(chatId, productId, quantity);
        return "Product added to cart successfully!";
    }

    @GetMapping("/load")
    public List<UserCart> getUserCart(@RequestParam long chatId) {
        return userCartService.getUserCartByChatId(chatId);
    }
}
