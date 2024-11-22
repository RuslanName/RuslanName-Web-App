package mainFiles.services;

import lombok.Getter;
import mainFiles.database.tables.userCarts.UserCart;
import mainFiles.database.tables.userCarts.UserCartsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WebService {
    @Autowired
    private UserCartsRepository userCartsRepository;

    @Autowired
    private DatabaseService databaseService;

    @Getter
    @Value("${web.url}")
    private String URL;

    @Getter
    @Value("${web.allow.editing}")
    private boolean allowEditing;

    @Getter
    @Value("${web.require.telegram.auth")
    private boolean requireTelegramUser;

    public void updateProductQuantity(long chatId, int productId, int quantityChange) {
        UserCart existingCart = userCartsRepository.findByChatIdAndProductId(chatId, productId);
        if (existingCart != null) {
            int newQuantity = existingCart.getQuantity() + quantityChange;

            if (newQuantity > 0) {
                existingCart.setQuantity(newQuantity);
                userCartsRepository.save(existingCart);
            } else {
                userCartsRepository.delete(existingCart);
                databaseService.updateDatabaseSequences("user_carts_data");
            }
        } else if (quantityChange > 0) {
            UserCart newCart = new UserCart();
            newCart.setChatId(chatId);
            newCart.setProductId(productId);
            newCart.setQuantity(quantityChange);
            newCart.setSelected(true);
            newCart.setId(databaseService.getNextId("user_carts_data"));

            userCartsRepository.save(newCart);
        }
    }

    public List<UserCart> getUserCartByChatId(long chatId) {
        return userCartsRepository.findByChatId(chatId);
    }

    public void updateProductSelection(long chatId, int productId, boolean selected) {
        UserCart existingCart = userCartsRepository.findByChatIdAndProductId(chatId, productId);
        if (existingCart != null) {
            existingCart.setSelected(selected);
            userCartsRepository.save(existingCart);
        }
    }
}
