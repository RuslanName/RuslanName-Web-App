package mainFiles.model.shop;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shop")
@PropertySource("application.properties")
public class ShopController {

    @Value("${webApp.url}")
    private String webAppUrl;
}

