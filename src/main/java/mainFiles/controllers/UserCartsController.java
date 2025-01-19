package mainFiles.controllers;

import mainFiles.database.tables.userCart.UserCart;
import mainFiles.services.WebService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user_carts_data")
@CrossOrigin(origins = "${web.url}")
public class UserCartsController {

    @Value("${web.url}")
    private String URL;

    @Autowired
    private WebService webService;

    @PostMapping("/update")
    public String addProductToCart(
            @RequestParam long chatId,
            @RequestParam int productId,
            @RequestParam int quantity) {

        webService.updateProductQuantity(chatId, productId, quantity);
        return "Product added to cart successfully!";
    }

    @PostMapping("/update_selection")
    public String updateProductSelection(
            @RequestParam long chatId,
            @RequestParam int productId,
            @RequestParam boolean selected) {

        webService.updateProductSelection(chatId, productId, selected);
        return "Product selection updated successfully!";
    }

    @GetMapping("/load")
    public List<UserCart> getUserCart(@RequestParam long chatId) {
        return webService.getUserCartByChatId(chatId);
    }
}