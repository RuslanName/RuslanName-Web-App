package mainFiles.services;

import mainFiles.configs.BotConfig;

import mainFiles.tables.user.User;
import mainFiles.tables.user.UsersRepository;
import mainFiles.tables.differentState.DifferentState;
import mainFiles.tables.differentState.DifferentStatesRepository;
import mainFiles.tables.product.ProductsRepository;
import mainFiles.tables.product.Product;
import mainFiles.tables.userCarts.UserCart;
import mainFiles.tables.userCarts.UserCartsRepository;
import mainFiles.tables.userOrders.UserOrdersRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
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
    private DatabaseService databaseService;

    final BotConfig config;

    static final String PRODUCT_KEYBOARD = "Действия с товарами";
    static final String USER_INFO_KEYBOARD = "Информация о пользователе";
    static final String ORDER_STATUS_KEYBOARD = "Cтатус заказа";
    static final String STATISTIC_KEYBOARD = "Статистика";
    static final String GIVE_ACCESS_RIGHTS_KEYBOARD = "Дать права доступа";

    static final String MAIN_MENU_KEYBOARD = "Главное меню";

    static final String ADD_PRODUCT_KEYBOARD = "Добавить";
    static final String VISIBILITY_PRODUCT_KEYBOARD = "Скрыть / Раскрыть";
    static final String DELETE_PRODUCT_KEYBOARD = "Удалить";
    static final String ADD_PRODUCT_QUANTITY_KEYBOARD = "Пополнить количество";
    static final String UPDATE_PRODUCT_QUANTITY_KEYBOARD = "Изменить количество";



    static final String HELP_TEXT = boldAndUnderline("СПИСОК КОМАНД") + "\n\n" +
            "/start - запустить бота \n" +
            "/registration - зарегестрироваться \n" +
            "/help - показать информацию о возможностях бота \n";

    static final String ACCESS_RIGHTS_TEXT = boldAndUnderline("ПРАВА ДОСТУПА") + "\n\n" +
            "1. Администратор \n" +
            "2. Пункт выдачи \n" +
            "3. Пользователь \n";

    public TelegramBot(BotConfig config) {
        this.config = config;
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "запуск бота"));
        listOfCommands.add(new BotCommand("/registration", "регистрация"));
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
        log.info("Received update: {}", update);

        if (update.hasMessage()) {
            Message message = update.getMessage();
            long chatId = message.getChatId();

            if (message.hasText()) {
                String text = message.getText();
                resetState(chatId, text);

                if (text.equals("/start")) {
                    start(chatId);
                }

                else if (text.equals("/help")) {
                    sendMessage(chatId, HELP_TEXT);
                }

                else if (text.equals("/registration")) {
                    if (userRepository.existsById(chatId)) {
                        sendMessage(chatId, "Вы уже зарегистрированы");
                    }

                    else {
                        setRegistrationState(chatId);
                        sendMessage(chatId, "Вам нужно зарегистрироваться");
                        sendMessage(chatId, "Введите свой российский номер телефона (без пробелов) или электронную почту");
                    }
                }

                else if (text.contains(MAIN_MENU_KEYBOARD) && (checkAccessRights(chatId, 2))) {
                    sendOwnerPanel(chatId);
                }

                else if (text.contains(ADD_PRODUCT_KEYBOARD) && (checkAccessRights(chatId, 2))) {
                    setProductState(chatId);
                    sendMessage(chatId, "Шаг 1. Введите имя товара");
                }

                else if (text.contains(VISIBILITY_PRODUCT_KEYBOARD) && (checkAccessRights(chatId, 2))) {
                    replaceVisibilityProductState(chatId);

                    String sendText = "";
                    var products = productsRepository.findAll();
                    sendText += boldAndUnderline("ТОВАРЫ") + "\n\n";

                    for (Product product : products) {
                        if (product.getVisibility()) {
                            sendText += product.getId() + ". " + product.getName() + " - " + "Раскрыт" + "\n";
                        }

                        else {
                            sendText += product.getId() + ". " + product.getName() + " - " + "Скрыт" + "\n";
                        }
                    }

                    if (sendText.isEmpty()) {
                        sendMessage(chatId, "Товары отсутствуют");
                    }

                    else {
                        sendMessage(chatId, sendText);
                        sendMessage(chatId, "Введите id товара");
                    }
                }

                else if (text.contains(DELETE_PRODUCT_KEYBOARD) && (checkAccessRights(chatId, 2))) {
                    setDeleteProductState(chatId);

                    String sendText = "";
                    var products = productsRepository.findAll();
                    sendText += boldAndUnderline("ТОВАРЫ") + "\n\n";

                    for (Product product : products) {
                        sendText += product.getId() + ". " + product.getName() + "\n";
                    }

                    if (sendText.isEmpty()) {
                        sendMessage(chatId, "Товары отсутствуют");
                    }

                    else {
                        sendMessage(chatId, sendText);
                        sendMessage(chatId, "Введите id товара");
                    }
                }

                else if (text.contains(ADD_PRODUCT_QUANTITY_KEYBOARD) && (checkAccessRights(chatId, 2))) {
                    setAddQuantityProductState(chatId);

                    String sendText = "";
                    var products = productsRepository.findAll();
                    sendText += boldAndUnderline("ТОВАРЫ") + "\n\n";

                    for (Product product : products) {
                        sendText += product.getId() + ". " + product.getName() + " - " + product.getQuantity() + "\n";
                    }

                    if (sendText.isEmpty()) {
                        sendMessage(chatId, "Товары отсутствуют");
                    }

                    else {
                        sendMessage(chatId, sendText);
                        sendMessage(chatId, "Шаг 1. Введите id товара");
                    }
                }

                else if (text.contains(UPDATE_PRODUCT_QUANTITY_KEYBOARD) && (checkAccessRights(chatId, 2))) {
                    setUpdateQuantityProductState(chatId);

                    String sendText = "";
                    var products = productsRepository.findAll();
                    sendText += boldAndUnderline("ТОВАРЫ") + "\n\n";

                    for (Product product : products) {
                        sendText += product.getId() + ". " + product.getName() + " - " + product.getQuantity() + "\n";
                    }

                    if (sendText.isEmpty()) {
                        sendMessage(chatId, "Товары отсутствуют");
                    }

                    else {
                        sendMessage(chatId, sendText);
                        sendMessage(chatId, "Шаг 1. Введите id товара");
                    }
                }

                else if (text.contains(ORDER_STATUS_KEYBOARD) && (checkAccessRights(chatId, 2))) {

                }

                else if (text.contains(USER_INFO_KEYBOARD) && (checkAccessRights(chatId, 2))) {

                }

                else if (text.contains(STATISTIC_KEYBOARD) && (checkAccessRights(chatId, 2))) {
                    String sendText = "";

                    if (productsRepository.existsById(1)) {
                        var products = productsRepository.findAll();
                        sendText += boldAndUnderline("ТОВАРЫ") + "\n\n";

                        for (Product product : products) {
                            sendText += product.getId() + ". " + product.getName() + " - " + product.getQuantity() + "\n";
                        }

                        sendText += "\n";
                    }

                    if (userCartsRepository.existsById(1)) {
                        var userCarts = userCartsRepository.findAll();
                        sendText += boldAndUnderline("КОРЗИНА") + "\n\n";

                        for (UserCart userCart : userCarts) {
                            sendText += userCart.getId() + ". " + userCart.getChatId() + " - " + userCart.getProductId() + " (" + userCart.getQuantity() + ")\n";
                        }
                    }

                    if (sendText.isEmpty()) {
                        sendMessage(chatId, "Товары отсутствуют");
                    }

                    else {
                        sendMessage(chatId, sendText);
                    }
                }

                else if (text.contains(GIVE_ACCESS_RIGHTS_KEYBOARD) && checkAccessRights(chatId, 1)) {
                    giveAccessRightsState(chatId);
                    sendMessage(chatId, "Шаг 1. Введите ChatId или Username пользователя");
                }

                else if (text.contains(PRODUCT_KEYBOARD) && (checkAccessRights(chatId, 2))) {
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(String.valueOf(chatId));
                    sendMessage.setText("Что вы хотите сделать?");

                    ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
                    replyKeyboardMarkup.setResizeKeyboard(true);

                    List<KeyboardRow> keyboardRows = new ArrayList<>();

                    var row_1 = new KeyboardRow();
                    row_1.add(ADD_PRODUCT_KEYBOARD);
                    row_1.add(VISIBILITY_PRODUCT_KEYBOARD);
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

                else if (differentStatesRepository.existsById(chatId)) {
                    if (differentStatesRepository.findById(chatId).get().getAction() == 1) {
                        registration(message, text);
                    }

                    else if (differentStatesRepository.findById(chatId).get().getAction() == 2) {
                        try {
                            addingProduct(message);
                        } catch (TelegramApiException e) {
                            throw new RuntimeException(e);
                        } catch (MalformedURLException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    else if (differentStatesRepository.findById(chatId).get().getAction() == 3) {
                        deleteProductDB(chatId, Integer.parseInt(text));
                    }

                    else if (differentStatesRepository.findById(chatId).get().getAction() == 4) {
                        replaceVisibilityProductDB(chatId, Integer.parseInt(text));
                    }

                    else if (differentStatesRepository.findById(chatId).get().getAction() == 5) {
                        addingQuantityProduct(chatId, text);
                    }

                    else if (differentStatesRepository.findById(chatId).get().getAction() == 6) {
                        updatingQuantityProduct(chatId, text);
                    }

                    else if (differentStatesRepository.findById(chatId).get().getAction() == 7) {
                        givingAccessRights(chatId, text);
                    }
                }

                else {
                    // Обработка, если ни одно условие не совпало
                }
            }

            else if (message.hasPhoto()) {
                if (differentStatesRepository.findById(chatId).get().getAction() == 2) {
                    try {
                        addingProduct(message);
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    } catch (MalformedURLException e) {
                        throw new RuntimeException(e);
                    }
                }

                else {
                    // Обработка, если ни одно условие не совпало
                }
            }
        }
    }

    private void start(long chatId) {
        if (userRepository.existsById(chatId)) {
            if (checkAccessRights(chatId, 1)) {
                sendOwnerPanel(chatId);
            }

            if (checkAccessRights(chatId, 2)) {
                sendAdministratorPanel(chatId);
            }

            else {
                User user = userRepository.findById(chatId).get();
                sendMessage(chatId, "Здравствуйте, " + user.getFirstName());
            }
        }

        else {
            setRegistrationState(chatId);
            sendMessage(chatId, "Здравствуйте! Вам нужно зарегистрироваться");
            sendMessage(chatId, "Введите свой российский номер телефона (без пробелов) или электронную почту");
        }
    }

    private void registration(Message message, String input) {
        long chatId = message.getChatId();
        String formattedInput = null;

        if (isValidPhoneNumber(input)) {
            formattedInput = formatPhoneNumber(input);
        } 
        
        else if (isValidEmail(input)) {
            formattedInput = input;
        }

        if (formattedInput != null) {
            addUserDB(message, formattedInput);
            sendMessage(chatId, "Регистрация завершена");

            if (checkAccessRights(chatId, 1)) {
                sendMessage(chatId, "Добро пожаловать, владелец " + message.getChat().getFirstName());
                sendOwnerPanel(chatId);
            }
        }
        
        else {
            sendMessage(chatId, "Номер телефона или электронная почта введены неправильно. Проверьте корректность ввода и введите снова");
        }
    }

    private void sendOwnerPanel(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));

        message.setText("Главное меню");

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

        row_3.add(GIVE_ACCESS_RIGHTS_KEYBOARD);

        keyboardRows.add(row_3);

        keyboardMarkup.setKeyboard(keyboardRows);
        message.setReplyMarkup(keyboardMarkup);

        executeFunction(message);
    }

    private void sendAdministratorPanel(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));

        message.setText("Главное меню");

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

        keyboardMarkup.setKeyboard(keyboardRows);
        message.setReplyMarkup(keyboardMarkup);

        executeFunction(message);
    }

    private void addingProduct(Message message) throws TelegramApiException, MalformedURLException {
        long chatId = message.getChatId();
        String text = message.getText();

        DifferentState differentState = differentStatesRepository.findById(chatId).get();

        if (differentState.getStep_1() == null) {
            differentState.setStep_1(text);
            differentStatesRepository.save(differentState);
            sendMessage(chatId, "Шаг 2. Введите количество товара");
        }

        else if (differentState.getStep_2() == null) {
            try {
                int quantity = Integer.parseInt(text);
                differentState.setStep_2(String.valueOf(quantity));
                differentStatesRepository.save(differentState);
                sendMessage(chatId, "Шаг 3. Введите цену товара");
            } catch (NumberFormatException e) {
                sendMessage(chatId, "Количество должно быть числом. Попробуйте еще раз");
            }
        }

        else if (differentState.getStep_3() == null) {
            try {
                int price = Integer.parseInt(text);
                differentState.setStep_3(String.valueOf(price));
                differentStatesRepository.save(differentState);
                sendMessage(chatId, "Шаг 4. Отправьте изображение товара");
            } catch (NumberFormatException e) {
                sendMessage(chatId, "Цена должна быть числом. Попробуйте еще раз");
            }
        }

        else {;
            addProductDB(message, differentState.getStep_1(), differentState.getStep_2(),
                    differentState.getStep_3());
        }
    }

    private void addingQuantityProduct(long chatId, String text) {
        DifferentState differentState = differentStatesRepository.findById(chatId).get();

        if (differentState.getStep_1() == null) {
            try {
                int id = Integer.parseInt(text);
                differentState.setStep_1(String.valueOf(id));
                differentStatesRepository.save(differentState);
                sendMessage(chatId, "Шаг 2. Введите количество товара");
            } catch (NumberFormatException e) {
                sendMessage(chatId, "ID должно быть числом. Попробуйте еще раз");
            }
        }

        else {
            try {
                int quantity = Integer.parseInt(text);
                addQuantityProductDB(chatId, Integer.parseInt(differentState.getStep_1()), quantity);
            } catch (NumberFormatException e) {
                sendMessage(chatId, "Количество должно быть числом. Попробуйте еще раз");
            }
        }
    }

    private void updatingQuantityProduct(long chatId, String text) {
        DifferentState differentState = differentStatesRepository.findById(chatId).get();

        if (differentState.getStep_1() == null) {
            try {
                int id = Integer.parseInt(text);
                differentState.setStep_1(String.valueOf(id));
                differentStatesRepository.save(differentState);
                sendMessage(chatId, "Шаг 2. Введите количество товара");
            } catch (NumberFormatException e) {
                sendMessage(chatId, "ID должно быть числом. Попробуйте еще раз");
            }
        }

        else {
            try {
                int quantity = Integer.parseInt(text);
                updateQuantityProductDB(chatId, Integer.parseInt(differentState.getStep_1()), quantity);
            } catch (NumberFormatException e) {
                sendMessage(chatId, "Количество должно быть числом. Попробуйте еще раз");
            }
        }
    }

    private void givingAccessRights(long chatId, String text) {
        DifferentState differentState = differentStatesRepository.findById(chatId).get();

        if (differentState.getStep_1() == null) {
            String formattedText = removeAtSymbol(text);

            if (isNumeric(text) && userRepository.existsById(Long.valueOf(text))) {
                differentState.setStep_1(text);
            }

            else if (userRepository.existsByUserName(formattedText)) {
                differentState.setStep_1(formattedText);
            }

            else {
                sendMessage(chatId, "ChatId или Username пользователя введены неправильно. Проверьте корректность ввода и введите снова");
                return;
            }

            differentStatesRepository.save(differentState);

            sendMessage(chatId, ACCESS_RIGHTS_TEXT);
            sendMessage(chatId, "Шаг 2. Введите id права доступа");
        }

        else {
            try {
                int id = Integer.parseInt(text);
                setAccessRights(chatId, differentState.getStep_1(), id);
            } catch (NumberFormatException e) {
                sendMessage(chatId, "Количество должно быть числом. Попробуйте еще раз");
            }
        }
    }

    private void setRegistrationState(long chatId) {
        if (!differentStatesRepository.existsById(chatId)) {
            DifferentState differentState = new DifferentState();
            differentState.setChatId(chatId);
            differentState.setAction(1);
            differentStatesRepository.save(differentState);
        }
    }

    private void setProductState(long chatId) {
        if (!differentStatesRepository.existsById(chatId)) {
            DifferentState differentState = new DifferentState();
            differentState.setChatId(chatId);
            differentState.setAction(2);
            differentStatesRepository.save(differentState);
        }
    }

    private void setDeleteProductState(long chatId) {
        if (!differentStatesRepository.existsById(chatId)) {
            DifferentState differentState = new DifferentState();
            differentState.setChatId(chatId);
            differentState.setAction(3);
            differentStatesRepository.save(differentState);
        }
    }

    private void replaceVisibilityProductState(long chatId) {
        if (!differentStatesRepository.existsById(chatId)) {
            DifferentState differentState = new DifferentState();
            differentState.setChatId(chatId);
            differentState.setAction(4);
            differentStatesRepository.save(differentState);
        }
    }

    private void setAddQuantityProductState(long chatId) {
        if (!differentStatesRepository.existsById(chatId)) {
            DifferentState differentState = new DifferentState();
            differentState.setChatId(chatId);
            differentState.setAction(5);
            differentStatesRepository.save(differentState);
        }
    }

    private void setUpdateQuantityProductState(long chatId) {
        if (!differentStatesRepository.existsById(chatId)) {
            DifferentState differentState = new DifferentState();
            differentState.setChatId(chatId);
            differentState.setAction(6);
            differentStatesRepository.save(differentState);
        }
    }

    private void giveAccessRightsState(long chatId) {
        if (!differentStatesRepository.existsById(chatId)) {
            DifferentState differentState = new DifferentState();
            differentState.setChatId(chatId);
            differentState.setAction(7);
            differentStatesRepository.save(differentState);
        }
    }

    private void addUserDB(Message message, String userData) {
        var chat = message.getChat();
        long chatId = message.getChatId();

        if (!userRepository.existsById(chatId)) {
            User user = new User();
            user.setChatId(chatId);
            user.setUserName(chat.getUserName());
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setUserData(userData);
            user.setAccessRights(3);
            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));

            userRepository.save(user);

            deleteState(chatId);
        }
    }

    private void addProductDB(Message message, String name, String quantity, String price) throws TelegramApiException, MalformedURLException {
        long chatId = message.getChatId();

        Product product = new Product();

        product.setId(databaseService.getNextId("products_data"));

        product.setName(name);
        product.setQuantity(Integer.parseInt(quantity));
        product.setPrice(Integer.parseInt(price));
        String iconPath = config.getProductIconsPath() + saveProductIcon(message);
        product.setIconPath(iconPath);
        product.setVisibility(true);

        productsRepository.save(product);

        sendMessage(chatId, "Товар добавлен");

        deleteState(chatId);
    }

    private void deleteProductDB(Long chatId, int id) {
        if (productsRepository.existsById(id)) {
            var product = productsRepository.findById(id).get();

            String iconPathToDelete = product.getIconPath();
            productsRepository.deleteById(id);

            try {
                Files.deleteIfExists(Path.of(iconPathToDelete));
            } catch (IOException e) {
                sendMessage(chatId, "Ошибка при удалении изображения: " + e.getMessage());
            }

            databaseService.updateDatabaseSequences("products_data");

            sendMessage(chatId, "Товар c ID " + id + " удалён");

            deleteState(chatId);
        }
    }

    private void replaceVisibilityProductDB(Long chatId, int id) {
        if (productsRepository.existsById(id)) {
            var product = productsRepository.findById(id).get();

            if (product.getVisibility()) {
                product.setVisibility(false);
                productsRepository.save(product);
                sendMessage(chatId, "Товар с ID " + id + " скрыт");
            } else {
                product.setVisibility(true);
                productsRepository.save(product);
                sendMessage(chatId, "Товар с ID " + id + " раскрыт");
            }

            deleteState(chatId);
        }
    }

    private void addQuantityProductDB(Long chatId, int id, int quantity) {
        if (productsRepository.existsById(id)) {
            var product = productsRepository.findById(id).get();

            product.setQuantity(quantity);
            productsRepository.save(product);
            sendMessage(chatId, "Количество товара с ID " + id + " увеличено на " + quantity);

            deleteState(chatId);
        }
    }

    private void updateQuantityProductDB(Long chatId, int id, int quantity) {
        if (productsRepository.existsById(id)) {
            var product = productsRepository.findById(id).get();

            product.setQuantity(quantity);
            productsRepository.save(product);
            sendMessage(chatId, "Количество товара с ID " + id + " изменено на " + quantity);

            deleteState(chatId);
        }
    }

    private void setAccessRights(Long chatId, String userIdentification, int id) {
        if (isNumeric(userIdentification)) {
            var user = userRepository.findById(Long.valueOf(userIdentification)).get();
            user.setAccessRights(id);
            userRepository.save(user);
            sendMessage(chatId, "Пользователю с ChatId " + userIdentification + " выданы права доступа " + id + " уровня");
        }

        else {
            var user = userRepository.findByUserName(userIdentification).get();
            user.setAccessRights(id);
            userRepository.save(user);
            sendMessage(chatId, "Пользователю @" + userIdentification + " выданы права доступа " + id + " уровня");
        }

        deleteState(chatId);
    }

    private boolean checkAccessRights(Long chatId, int clearanceLevel) {
        if (clearanceLevel == 1 && config.getOwnerChatId().equals(chatId)) {
            return true;
        }

        else if (clearanceLevel == 2 && (userRepository.findById(chatId).get().getAccessRights() == 1 || config.getOwnerChatId().equals(chatId))) {
            return true;
        }

        else if (clearanceLevel == 3 && (userRepository.findById(chatId).get().getAccessRights() <= 2 || config.getOwnerChatId().equals(chatId))){
            return true;
        }

        return false;
    }

    private void deleteState(Long chatId) {
        if (differentStatesRepository.existsById(chatId)) {
            differentStatesRepository.deleteById(chatId);
        }
    }

    private void resetState(long chatId, String messageText) {
        if (userRepository.existsById(chatId)) {
            if ((checkAccessRights(chatId, 2) && (messageText.equals(PRODUCT_KEYBOARD) ||
                    messageText.equals(USER_INFO_KEYBOARD) || messageText.equals(ORDER_STATUS_KEYBOARD) ||
                    messageText.equals(STATISTIC_KEYBOARD) || messageText.equals(MAIN_MENU_KEYBOARD) ||
                    messageText.equals(ADD_PRODUCT_KEYBOARD) || messageText.equals(VISIBILITY_PRODUCT_KEYBOARD) ||
                    messageText.equals(DELETE_PRODUCT_KEYBOARD) || messageText.equals(ADD_PRODUCT_QUANTITY_KEYBOARD) ||
                    messageText.equals(UPDATE_PRODUCT_QUANTITY_KEYBOARD))) || messageText.equals("/start") ||
                    messageText.equals("/registration") || messageText.equals("/help")) {
                deleteState(chatId);
            }
        }
    }

    private String saveProductIcon(Message message) throws TelegramApiException, MalformedURLException {
        String iconFileName = "icon_" + System.currentTimeMillis() + ".jpg";
        String iconPath = config.getProductIconsPath() + iconFileName;

        String fileId = message.getPhoto().get(message.getPhoto().size() - 1).getFileId();
        File file = this.execute(new GetFile(fileId));
        String fileUrl = "https://api.telegram.org/file/bot" + getBotToken() + "/" + file.getFilePath();

        try (InputStream in = new URL(fileUrl).openStream()) {
            Files.copy(in, Path.of(iconPath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return iconFileName;
    }

    private boolean isValidPhoneNumber(String input) {
        if (input.matches("^(\\+7|8)?\\d{9}$")) {
            return true;
        }
        return false;
    }

    private String formatPhoneNumber(String input) {
        if (input.startsWith("8")) {
            input = "+7" + input.substring(1);
        } else if (!input.startsWith("+7")) {
            input = "+7" + input;
        }
        return input;
    }

    private boolean isValidEmail(String input) {
        return input.matches("^[\\w-.]+@[\\w-]+\\.[a-z]{2,}$");
    }

    public boolean isNumeric(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        try {
            Long.parseLong(text);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static String removeAtSymbol(String text) {
        return text.replace("@", "");
    }

    private static String bold(String text) {
        return "<b>%s</b>".formatted(text);
    }

    private static String italic(String text) {
        return "<i>%s</i>".formatted(text);
    }

    private static String boldAndUnderline(String text) {
        return "<b><u>%s</u></b>".formatted(text);
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
