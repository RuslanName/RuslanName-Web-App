package mainFiles.service;

import mainFiles.model.userCarts.UserCart;
import mainFiles.model.userCarts.UserCartsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserCartService {

    @Autowired
    private UserCartsRepository userCartsRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // public void updateProductQuantity(long chatId, int productId, int quantityChange) {
    //     // Проверяем, есть ли уже такой продукт в корзине пользователя
    //     UserCart existingCart = userCartsRepository.findByChatIdAndProductId(chatId, productId);
    //     if (existingCart != null) {
    //         int newQuantity = existingCart.getQuantity() + quantityChange;
    
    //         if (newQuantity > 0) {
    //             // Если количество больше 0, обновляем количество
    //             existingCart.setQuantity(newQuantity);
    //             userCartsRepository.save(existingCart);
    //         } else {
    //             // Если количество стало 0 или меньше, удаляем товар из корзины
    //             userCartsRepository.delete(existingCart);
    //             // Обновляем последовательности, если нужно
    //             updateDatabaseSequences("user_carts_data");
    //         }
    //     } else if (quantityChange > 0) {
    //         // Если товара нет и мы хотим добавить количество, создаем новую запись
    //         UserCart newCart = new UserCart();
    //         newCart.setChatId(chatId);
    //         newCart.setProductId(productId);
    //         newCart.setQuantity(quantityChange); // Добавляем количество
    
    //         // Получаем максимальный ID
    //         if (userCartsRepository.findById(1).isEmpty()) {
    //             newCart.setId(1);
    //         } else {
    //             var carts = userCartsRepository.findAll();
    //             int maxId = 0;
    
    //             for (UserCart cart : carts) {
    //                 if (cart.getId() > maxId) {
    //                     maxId = cart.getId();
    //                 }
    //             }
    //             newCart.setId(maxId + 1);
    //         }
    
    //         userCartsRepository.save(newCart);
    //     }
    // }

        public void updateProductQuantity(long chatId, int productId, int quantityChange) {
        UserCart existingCart = userCartsRepository.findByChatIdAndProductId(chatId, productId);
        if (existingCart != null) {
            int newQuantity = existingCart.getQuantity() + quantityChange;
    
            if (newQuantity > 0) {
                existingCart.setQuantity(newQuantity);
                userCartsRepository.save(existingCart);
            } 
                
            else {
                userCartsRepository.delete(existingCart);
                updateDatabaseSequences("user_carts_data");
            }
        } 
        
        else if (quantityChange > 0) {
            UserCart newCart = new UserCart();
            newCart.setChatId(chatId);
            newCart.setProductId(productId);
            newCart.setQuantity(quantityChange);
    
            if (userCartsRepository.findById(1).isEmpty()) {
                newCart.setId(1);
            }
            
            else {
                var carts = userCartsRepository.findAll();
                int maxId = 0;
    
                for (UserCart cart : carts) {
                    if (cart.getId() > maxId) {
                        maxId = cart.getId();
                    }
                }
                newCart.setId(maxId + 1);
            }
    
            userCartsRepository.save(newCart);
        }
    }

    public List<UserCart> getUserCartByChatId(long chatId) {
        return userCartsRepository.findByChatId(chatId);
    }

    private void updateDatabaseSequences(String tableName) {
        String tempTable = "temp_" + tableName;
        String createTempTableQuery = String.format(
                "CREATE TEMP TABLE %s AS " +
                        "SELECT id, ROW_NUMBER() OVER (ORDER BY id) AS new_id " +
                        "FROM %s;", tempTable, tableName);
        jdbcTemplate.execute(createTempTableQuery);

        String updateQuery = String.format(
                "UPDATE %s " +
                        "SET id = (SELECT new_id FROM %s WHERE %s.id = %s.id);",
                tableName, tempTable, tableName, tempTable);
        jdbcTemplate.execute(updateQuery);

        String dropTempTableQuery = String.format("DROP TABLE %s;", tempTable);
        jdbcTemplate.execute(dropTempTableQuery);

        String resetSequenceQuery = String.format(
                "UPDATE %s SET id = (SELECT COALESCE(MAX(id), 0) + ROW_NUMBER() OVER (ORDER BY id) FROM %s) WHERE id > 0;",
                tableName, tableName);
        jdbcTemplate.execute(resetSequenceQuery);
    }
}
