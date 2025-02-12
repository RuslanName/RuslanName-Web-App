package mainFiles.controllers;

import mainFiles.configs.DTOConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/config")
public class MainController {

    @Value("${web.url}")
    private String url;

    @Value("${product.iconsPath}")
    private String productIconsPath;

    @Value("${web.allowEditing}")
    private boolean allowEditing;

    @Value("${web.requireAuthorization}")
    private boolean requireAuthorization;

    @GetMapping
    public DTOConfig getConfig() {
        return new DTOConfig(url, productIconsPath, allowEditing, requireAuthorization);
    }
}