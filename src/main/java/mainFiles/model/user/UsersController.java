package mainFiles.model.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "https://magazin-ruslanname.amvera.io")
public class UsersController {

    @Autowired
    private UsersRepository userRepository;

    @GetMapping("/check_user")
    public Map<String, Boolean> checkUserRegistration(@RequestParam("chatId") String chatId) {
        Optional<User> user = userRepository.findById(Long.valueOf(chatId));

        Map<String, Boolean> response = new HashMap<>();
        response.put("isRegistered", user.isPresent());

        return response;
    }
}
