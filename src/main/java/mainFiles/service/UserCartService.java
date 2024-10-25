package mainFiles.service;

import mainFiles.model.userCarts.UserCart;
import mainFiles.model.userCarts.UserCartsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserCartService {

    @Autowired
    private UserCartsRepository userCartsRepository;

    public void addProductToCart(long chatId, int productId, int quantity) {
        // Проверяем, есть ли уже такой продукт в корзине пользователя
        UserCart existingCart = userCartsRepository.findByChatIdAndProductId(chatId, productId);
        if (existingCart != null) {
            // Если товар уже есть, обновляем количество
            existingCart.setQuantity(existingCart.getQuantity() + quantity);
            userCartsRepository.save(existingCart);
        } else {
            // Если товара нет, создаем новую запись
            UserCart newCart = new UserCart();
            
            // Получаем максимальный ID
            if (userCartsRepository.findById(1).isEmpty()) {
                newCart.setId(1);
            } else {
                var carts = userCartsRepository.findAll();
                int maxId = 0;

                for (UserCart cart : carts) {
                    if (cart.getId() > maxId) {
                        maxId = cart.getId();
                    }
                }
                newCart.setId(maxId + 1); // Устанавливаем новый ID
            }

            newCart.setChatId(chatId);
            newCart.setProductId(productId);
            newCart.setQuantity(quantity);
            userCartsRepository.save(newCart);
        }
    }

    // Дополнительные методы для работы с корзиной, если нужно
}
