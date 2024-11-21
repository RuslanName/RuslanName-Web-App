package mainFiles.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TemplateController {

    @Value("${web.url}")
    private String webURL;

    @GetMapping("/cart")
    public String getCartPage(Model model) {
        model.addAttribute("webUrl", webURL);
        return "cart";
    }
}
