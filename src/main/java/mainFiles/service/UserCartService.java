@Service
public class UserCartService {

    @Autowired
    private UserCartsRepository userCartsRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void updateProductQuantity(long chatId, int productId, int quantityChange) {
    // Проверяем, есть ли уже такой продукт в корзине пользователя
    UserCart existingCart = userCartsRepository.findByChatIdAndProductId(chatId, productId);
    
    if (existingCart != null) {
        int newQuantity = Math.max(0, quantityChange); 

        if (newQuantity > 0) {
            // Если количество больше 0, обновляем количество
            existingCart.setQuantity(newQuantity);
            userCartsRepository.save(existingCart);
        } else {
            // Если количество стало 0 или меньше, удаляем товар из корзины
            userCartsRepository.delete(existingCart);
            // Обновляем последовательности, если нужно
            updateDatabaseSequences("user_carts_data");
        }
    } 
    
    else if (quantityChange > 0) {
        // Если товара нет и мы хотим добавить количество, создаем новую запись
        UserCart newCart = new UserCart();
        newCart.setChatId(chatId);
        newCart.setProductId(productId);
        newCart.setQuantity(quantityChange);

        // Получаем максимальный ID
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
            newCart.setId(maxId + 1); // Устанавливаем новый ID
        }

        userCartsRepository.save(newCart);
    }
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

    // Дополнительные методы для работы с корзиной, если нужно
}
