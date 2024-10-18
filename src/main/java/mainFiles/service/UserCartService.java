package mainFiles.service;

import mainFiles.model.userCarts.UserCart;
import mainFiles.model.userCarts.UserCartsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
            newCart.setChatId(chatId);
            newCart.setProductId(productId);
            newCart.setQuantity(quantity);
            userCartsRepository.save(newCart);
        }
    }

    // Дополнительные методы для работы с корзиной, если нужно
}

