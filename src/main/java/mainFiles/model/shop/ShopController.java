package mainFiles.model.shop;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/shop")
public class ShopController {

    @Value("${shop.url}")
    private String shopUrl;

    @GetMapping("/info")
    public Map<String, String> getShopInfo() {
        return Map.of(
                "message", "Добро пожаловать в магазин!",
                "shopUrl", shopUrl
        );
    }
}

