package mainFiles.service;

import mainFiles.model.userCarts.UserCart;
import mainFiles.model.userCarts.UserCartsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserCartService {

    @Autowired
    private UserCartsRepository userCartsRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

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
            newCart.setSelected(true);
            newCart.setId(getNextId("user_carts_data"));

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

    @Transactional
    private int getNextId(String tableName) {
        String selectMaxIdQuery = String.format("SELECT COALESCE(MAX(id), 0) + 1 FROM %s;", tableName);
        return jdbcTemplate.queryForObject(selectMaxIdQuery, Integer.class);
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
