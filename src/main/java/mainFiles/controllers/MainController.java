package mainFiles.controllers;

import mainFiles.services.TelegramBot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {
    @Autowired
    private TelegramBot telegramBot;
    
    @Value("${web.url}")
    private String URL;

    @Value("${web.allow.editing}")
    private boolean allowEditing;

    @Value("${web.require.telegram.auth}")
    private boolean requireTelegramUser;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("URL", URL);
        model.addAttribute("allowEditing", allowEditing);
        model.addAttribute("requireTelegramUser", requireTelegramUser);

        long chatId = 1836263458L;

        String messageText = "URL: " + URL + "\n" +
                "allowEditing: " + allowEditing + "\n" +
                "requireTelegramUser: " + requireTelegramUser;
        
        telegramBot.sendMessage(chatId, messageText);

        return "index";
    }

}

