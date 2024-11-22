package mainFiles.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);
    
    @Value("${web.url}")
    private String URL;

    @Value("${web.allow.editing}")
    private boolean allowEditing;

    @Value("${web.require.telegram.auth}")
    private boolean requireTelegramUser;

    @GetMapping("/")
    public String index(Model model) {
        logger.info("URL: {}", URL);
        logger.info("Allow Editing: {}", allowEditing);
        logger.info("Require Telegram User: {}", requireTelegramUser);

        model.addAttribute("URL", URL);
        model.addAttribute("allowEditing", allowEditing);
        model.addAttribute("requireTelegramUser", requireTelegramUser);
        return "index";
    }

}

