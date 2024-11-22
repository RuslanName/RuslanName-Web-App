package mainFiles.mainController;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class mainController {
    @Value("${web.url}")
    private String webUrl;

    @Value("${app.allow.editing}")
    private boolean allowEditing;

    @Value("${app.require.telegram.user}")
    private boolean requireTelegramUser;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("webUrl", webUrl);
        model.addAttribute("allowEditing", allowEditing);
        model.addAttribute("requireTelegramUser", requireTelegramUser);
        return "index";
    }
}

