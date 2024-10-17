package mainFiles.controller;

import mainFiles.service.UserCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private UserCartService userCartService;

    @PostMapping("/add")
    public String addProductToCart(@RequestParam long chatId, @RequestParam int productId, @RequestParam int quantity) {
        userCartService.addProductToCart(chatId, productId, quantity);
        return "Product added to cart successfully!";
    }
}

