package mainFiles.model.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")  // Укажите путь к вашему API
                .allowedOrigins("https://magazin-ruslanname.amvera.io") // Разрешить только этот домен
                .allowedMethods("GET", "POST", "PUT", "DELETE")  // Укажите методы, которые разрешены
                .allowedHeaders("*") // Разрешить все заголовки
                .allowCredentials(true); // Разрешить отправку cookie (если нужно)
    }
}
