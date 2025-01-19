package mainFiles.controllers;

import mainFiles.database.tables.user.User;
import mainFiles.database.tables.user.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class UsersController {
    @Value("${web.url}")
    private String URL;

    @Autowired
    private UsersRepository userRepository;

    @GetMapping("/check_user")
    @CrossOrigin(origins = "${web.url}")
    public Map<String, Boolean> checkUserRegistration(@RequestParam String chatId) {
        Optional<User> user = userRepository.findById(Long.valueOf(chatId));

        Map<String, Boolean> response = new HashMap<>();
        response.put("isRegistered", user.isPresent());

        return response;
    }
}

