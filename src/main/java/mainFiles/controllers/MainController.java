package mainFiles.controllers;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {
    @Value("${web.url}")
    private String URL;

    @Value("${web.allow.editing}")
    private boolean allowEditing;

    @Value("${web.require.telegram.auth}")
    private boolean requireTelegramUser;

    @GetMapping("/")
    public String index(Model model) {
        System.out.println("URL: " + URL);
        System.out.println("allowEditing: " + allowEditing);
        System.out.println("requireTelegramUser: " + requireTelegramUser);
        model.addAttribute("URL", URL);
        model.addAttribute("allowEditing", allowEditing);
        model.addAttribute("requireTelegramUser", requireTelegramUser);
        return "index";
    }
}
