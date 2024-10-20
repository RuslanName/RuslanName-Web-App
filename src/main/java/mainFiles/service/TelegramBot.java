package mainFiles.service;

import mainFiles.config.BotConfig;

import mainFiles.model.user.User;
import mainFiles.model.user.UsersRepository;
import mainFiles.model.differentState.DifferentState;
import mainFiles.model.differentState.DifferentStatesRepository;
import mainFiles.model.product.ProductsRepository;
import mainFiles.model.product.Product;
import mainFiles.model.userCarts.UserCart;
import mainFiles.model.userCarts.UserCartsRepository;
import mainFiles.model.userOrders.UserOrdersRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.api.objects.webapp.WebAppInfo;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;



@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    @Autowired
    private UsersRepository userRepository;

    @Autowired
    private DifferentStatesRepository differentStatesRepository;

    @Autowired
    private ProductsRepository productsRepository;

    @Autowired
    private UserCartsRepository userCartsRepository;

    @Autowired
    private UserOrdersRepository userOrdersRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    final BotConfig config;

    static final String PRODUCT_KEYBOARD = "Действия с товарами";
    static final String USER_INFO_KEYBOARD = "Информация о пользователе";
    static final String ORDER_STATUS_KEYBOARD = "Cтатус заказа";
    static final String STATISTIC_KEYBOARD = "Статистика";

    static final String MAIN_MENU_KEYBOARD = "Главное меню";

    static final String ADD_PRODUCT_KEYBOARD = "Добавить";
    static final String HIDE_PRODUCT_KEYBOARD = "Скрыть";
    static final String DELETE_PRODUCT_KEYBOARD = "Удалить";
    static final String ADD_PRODUCT_QUANTITY_KEYBOARD = "Пополнить количество";
    static final String UPDATE_PRODUCT_QUANTITY_KEYBOARD = "Изменить количество";

    static final String SHOP_KEYBOARD = "Магазин";

    static final String HELP_TEXT = boldAndUnderline("СПИСОК КОМАНД") + "\n\n" +
            "/start - запустить бота \n" +
            "/help - показать информацию о возможностях бота \n";

    public TelegramBot(BotConfig config) {
        this.config = config;
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "запуск бота"));
        listOfCommands.add(new BotCommand("/help", "информация о возможностях бота"));
        try {
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error setting bot's command list: " + e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            String messageText = update.getMessage().getText();
            long chatId = message.getChatId();
            String text = message.getText();

            resetState(chatId, text);

            if (text.equals("/start")) {
                start(chatId);
            }

            else if (messageText.contains(MAIN_MENU_KEYBOARD) && config.getAdminChatId() == chatId) {
                sendAdminPanel(chatId, "Главное меню");
            }

            else if (differentStatesRepository.existsById(chatId)) {
                if (differentStatesRepository.findById(chatId).get().getAction().equals("Registration")) {
                    if (differentStatesRepository.findById(chatId).get().getUserName() == null) {
                        registration(chatId, "Name", text);
                    }

                    else if (differentStatesRepository.findById(chatId).get().getUserPhoneNumber() == null) {
                        registration(chatId, "PhoneNumber", text);
                    }
                }

                else if (differentStatesRepository.findById(chatId).get().getAction().equals("AddProduct")) {
                    if (differentStatesRepository.findById(chatId).get().getProductName() == null) {
                        addProduct(chatId, "Name", text);
                    }

                    else if (differentStatesRepository.findById(chatId).get().getProductQuantity() == null) {
                        addProduct(chatId, "Quantity", text);
                    }

                    else if (differentStatesRepository.findById(chatId).get().getProductPrice() == null) {
                        addProduct(chatId, "Price", text);
                    }
                }

                else if (differentStatesRepository.findById(chatId).get().getAction().equals("DeleteProduct")) {
                    deleteProduct(chatId, text);
                }
            }

            else if (messageText.contains(PRODUCT_KEYBOARD) && config.getAdminChatId() == chatId) {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(String.valueOf(chatId));

                sendMessage.setText("Что вы хотите сделать?");

                ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
                replyKeyboardMarkup.setResizeKeyboard(true);

                List<KeyboardRow> keyboardRows = new ArrayList<>();

                var row_1 = new KeyboardRow();

                row_1.add(ADD_PRODUCT_KEYBOARD);
                row_1.add(HIDE_PRODUCT_KEYBOARD);
                row_1.add(DELETE_PRODUCT_KEYBOARD);

                keyboardRows.add(row_1);

                var row_2 = new KeyboardRow();

                row_2.add(ADD_PRODUCT_QUANTITY_KEYBOARD);
                row_2.add(UPDATE_PRODUCT_QUANTITY_KEYBOARD);

                keyboardRows.add(row_2);

                var row_3 = new KeyboardRow();

                row_3.add(MAIN_MENU_KEYBOARD);

                keyboardRows.add(row_3);

                replyKeyboardMarkup.setKeyboard(keyboardRows);
                sendMessage.setReplyMarkup(replyKeyboardMarkup);

                executeFunction(sendMessage);
            }

            else if (messageText.contains(ADD_PRODUCT_KEYBOARD) && config.getAdminChatId() == chatId) {
                addProduct(chatId);
                sendMessage(chatId, "Шаг 1. Введите имя товара");
            }

            else if (messageText.contains(HIDE_PRODUCT_KEYBOARD) && config.getAdminChatId() == chatId) {
            }

            else if (messageText.contains(DELETE_PRODUCT_KEYBOARD) && config.getAdminChatId() == chatId) {
                deleteProduct(chatId);
                sendMessage(chatId, "Введите имя товара");
            }

            else if (messageText.contains(ORDER_STATUS_KEYBOARD) && config.getAdminChatId() == chatId) {
            }

            else if (messageText.contains(USER_INFO_KEYBOARD) && config.getAdminChatId() == chatId) {
            }

            else if (messageText.contains(STATISTIC_KEYBOARD) && config.getAdminChatId() == chatId) {
                String sendText = "";

                if (productsRepository.findById(1).isPresent()) {
                    var products = productsRepository.findAll();
                    sendText += boldAndUnderline("ТОВАРЫ") + "\n\n";

                    for (Product product : products) {
                        sendText += product.getId() + ". " + product.getName() + " - " + product.getQuantity() + "\n";
                    }

                    sendText += "\n";
                }

                if (userCartsRepository.findById(1).isPresent()) {
                    var userCarts = userCartsRepository.findAll();
                    sendText += boldAndUnderline("КОРЗИНА") + "\n\n";

                    for (UserCart userCart : userCarts) {
                        sendText += userCart.getId() + ". " + userCart.getChatId() + " - " + userCart.getProductId() + " (" + userCart.getQuantity() + ")\n";
                    }
                }

                sendMessage(chatId, sendText);
            }

            else {
//                sendMessage();
            }
        }
    }

    private void start(long chatId) {
        if (userRepository.findById(chatId).isPresent()) {
            if (chatId == config.getAdminChatId()) {
                sendAdminPanel(chatId, "Старт");
            }

            else {
                User user = userRepository.findById(chatId).get();
                sendMessage(chatId, "Здравствуйте, " + user.getUsername());
                sendShopButton(chatId);
            }
        }

        else {
            registration(chatId);
            sendMessage(chatId, "Здравствуйте! Вам нужно зарегистрироваться");
            sendMessage(chatId, "Шаг 1. Введите ваше имя (никнейм)");
        }
    }

    private void registration(long chatId) {
        if (!differentStatesRepository.existsById(chatId)) {
            DifferentState differentState = new DifferentState();
            differentState.setChatId(chatId);
            differentState.setAction("Registration");
            differentStatesRepository.save(differentState);
        }
    }

    private void registration(long chatId, String step, String input) {
        DifferentState differentState = differentStatesRepository.findById(chatId).get();

        if (step.equals("Name")) {
            differentState.setUserName(input);
            differentStatesRepository.save(differentState);
            sendMessage(chatId, "Шаг 2. Введите свой номер телефона без пробелов (начало +7 или 8)");
        }

        else if (step.equals("PhoneNumber")) {
            if (!((input.startsWith("+7") || input.startsWith("8")) && input.length() > 11)) {
                sendMessage(chatId, "Номер введен неправильно. Введите правильный номер");
                return;
            }

            differentState.setUserPhoneNumber(input);
            differentStatesRepository.save(differentState);

            addUserDB(chatId, differentState.getUserName(), differentState.getUserPhoneNumber());

            if (chatId == config.getAdminChatId()) {
                sendAdminPanel(chatId, "Старт");
            }

            else {
                sendShopButton(chatId);
            }
        }
    }

    private void addProduct(long chatId) {
        if (!differentStatesRepository.existsById(chatId)) {
            DifferentState differentState = new DifferentState();
            differentState.setChatId(chatId);
            differentState.setAction("AddProduct");
            differentStatesRepository.save(differentState);
        }
    }

    private void addProduct(long chatId, String step, String input) {
        DifferentState differentState = differentStatesRepository.findById(chatId).get();

        if (step.equals("Name")) {
            differentState.setProductName(input);
            differentStatesRepository.save(differentState);
            sendMessage(chatId, "Шаг 2. Введите количество товара");
        }

        else if (step.equals("Quantity")) {
            try {
                int quantity = Integer.parseInt(input);
                differentState.setProductQuantity(quantity);
                differentStatesRepository.save(differentState);
                sendMessage(chatId, "Шаг 3. Введите цену товара");
            } catch (NumberFormatException e) {
                sendMessage(chatId, "Количество должно быть числом. Попробуйте еще раз");
            }
        }

        else if (step.equals("Price")) {
            try {
                int price = Integer.parseInt(input);
                differentState.setProductPrice(price);
                differentStatesRepository.save(differentState);

                addProductDB(differentState.getChatId(), differentState.getProductName(), differentState.getProductQuantity(), differentState.getProductPrice());

                sendMessage(chatId, "Товар успешно добавлен!");
            } catch (NumberFormatException e) {
                sendMessage(chatId, "Цена должна быть числом. Попробуйте еще раз");
            }
        }
    }

    private void deleteProduct(long chatId) {
        if (!differentStatesRepository.existsById(chatId)) {
            DifferentState differentState = new DifferentState();
            differentState.setChatId(chatId);
            differentState.setAction("DeleteProduct");
            differentStatesRepository.save(differentState);
        }
    }

    private void deleteProduct(Long chatId, String text) {
        var products = productsRepository.findAll();

        for (Product product : products) {
            if (product.getName().equals(text)) {
                productsRepository.deleteById(product.getId());
            }

            deleteState(chatId);
            updateDatabaseSequences("products_data");

            sendMessage(chatId, "Товар успешно удалён!");
        }
    }

    private void sendShopButton(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Перейдите в магазин");

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);

        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();

        KeyboardButton shopButton = new KeyboardButton(SHOP_KEYBOARD);
        shopButton.setWebApp(new WebAppInfo("https://magazin-ruslanname.amvera.io"));

        row.add(shopButton);
        keyboardRows.add(row);

        replyKeyboardMarkup.setKeyboard(keyboardRows);
        message.setReplyMarkup(replyKeyboardMarkup);

        executeFunction(message);
    }

    private void sendAdminPanel(long chatId, String action) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));

        User user = userRepository.findById(chatId).get();

        if (action.equals("Старт")) {
            message.setText("Добро пожаловать, администратор " + user.getUsername());
        }

        else {
            message.setText("Главное меню");
        }

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);

        List<KeyboardRow> keyboardRows = new ArrayList<>();

        var row_1 = new KeyboardRow();

        row_1.add(PRODUCT_KEYBOARD);
        row_1.add(ORDER_STATUS_KEYBOARD);

        keyboardRows.add(row_1);

        var row_2 = new KeyboardRow();

        row_2.add(USER_INFO_KEYBOARD);
        row_2.add(STATISTIC_KEYBOARD);

        keyboardRows.add(row_2);

        var row_3 = new KeyboardRow();

        KeyboardButton shopButton = new KeyboardButton(SHOP_KEYBOARD);
        shopButton.setWebApp(new WebAppInfo("https://magazin-ruslanname.amvera.io"));
        row_3.add(shopButton);

        keyboardRows.add(row_3);

        keyboardMarkup.setKeyboard(keyboardRows);
        message.setReplyMarkup(keyboardMarkup);

        executeFunction(message);
    }

    private void addUserDB(long chatId, String userName, String phoneNumber) {
        if (userRepository.findById(chatId).isEmpty()) {
            User user = new User();
            user.setChatId(chatId);
            user.setUsername(userName);
            user.setPhoneNumber(phoneNumber);
            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));

            userRepository.save(user);

            deleteState(chatId);
        }
    }

    private void addProductDB(long chatId, String name, int quantity, int price) {
            Product product = new Product();

            if (productsRepository.findById(1).isEmpty()) {
                product.setId(1);
            }

            else {
                var products = productsRepository.findAll();
                int i = 0;

                for (Product products_i : products) {
                    i++;
                }

                product.setId(i + 1);
            }

            product.setName(name);
            product.setQuantity(quantity);
            product.setPrice(price);

            productsRepository.save(product);

        deleteState(chatId);
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

    private void deleteState(Long chatId) {
        if (differentStatesRepository.existsById(chatId)) {
            differentStatesRepository.deleteById(chatId);
        }
    }

    private void resetState(long chatId, String messageText) {
        if ((config.getAdminChatId() == chatId && (messageText.equals(PRODUCT_KEYBOARD) ||
                messageText.equals(USER_INFO_KEYBOARD) || messageText.equals(ORDER_STATUS_KEYBOARD) ||
                messageText.equals(STATISTIC_KEYBOARD) || messageText.equals(MAIN_MENU_KEYBOARD) ||
                messageText.equals(ADD_PRODUCT_KEYBOARD) || messageText.equals(HIDE_PRODUCT_KEYBOARD) ||
                messageText.equals(DELETE_PRODUCT_KEYBOARD) || messageText.equals(ADD_PRODUCT_QUANTITY_KEYBOARD) ||
                messageText.equals(UPDATE_PRODUCT_QUANTITY_KEYBOARD))) || messageText.equals("/start") ||
                messageText.equals("/help")) {
            deleteState(chatId);
        }
    }

    private static String bold(String text) {
        return String.format("<b>%s</b>", text);
    }

    private static String italic(String text) {
        return String.format("<i>%s</i>", text);
    }

    private static String boldAndUnderline(String text) {
        return String.format("<b><u>%s</u></b>", text);
    }

    private void sendMessage(long chatId, Object text) {
        SendMessage message = new SendMessage();
        message.enableHtml(true);
        message.setChatId(String.valueOf(chatId));
        message.setText(text.toString());

        executeFunction(message);
    }

    private void executeFunction(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error: " + e.getMessage());
        }
    }
}
