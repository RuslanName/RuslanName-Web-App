package mainFiles.controllers;

import mainFiles.configs.DTOConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {

    @Value("${web.url}")
    private String url;

    @Value("${web.allow.editing}")
    private boolean allowEditing;

    @Value("${web.require.telegram.auth}")
    private boolean requireTelegramAuth;

    @GetMapping("/api/config")
    public DTOConfig getConfig() {
        return new DTOConfig(url, allowEditing, requireTelegramAuth);
    }
}

