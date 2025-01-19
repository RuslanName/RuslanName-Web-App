package mainFiles.services;

import mainFiles.configs.BotConfig;

import mainFiles.database.tables.user.User;
import mainFiles.database.tables.user.UsersRepository;
import mainFiles.database.tables.differentState.DifferentState;
import mainFiles.database.tables.differentState.DifferentStatesRepository;
import mainFiles.database.tables.product.ProductsRepository;
import mainFiles.database.tables.product.Product;
import mainFiles.database.tables.userCarts.UserCartsRepository;
import mainFiles.database.tables.userOrders.UserOrdersRepository;
import lombok.extern.slf4j.Slf4j;
import mainFiles.services.enums.AccessRights;
import mainFiles.services.enums.ActionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
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
import java.util.*;

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

    static final String MAIN_MENU_KEYBOARD = "Главное меню";

    static final String PRODUCTS_KEYBOARD = "Товары";
    static final String USERS_KEYBOARD = "Пользователи";
    static final String PICKUP_POINTS_KEYBOARD = "Пункты выдачи";
    static final String STATISTIC_KEYBOARD = "Статистика";
    static final String ACCESS_RIGHTS_KEYBOARD = "Дать права доступа";

    static final String PRODUCTS_INFO_KEYBOARD = "Общая информация";
    static final String PRODUCTS_ADD_PRODUCT_KEYBOARD = "Добавить";
    static final String PRODUCTS_PRODUCT_KEYBOARD = "Товар";

    static final String PRODUCT_INFO_KEYBOARD = "Информация";
    static final String PRODUCT_DELETE_KEYBOARD = "Удалить";
    static final String PRODUCT_VISIBILITY_KEYBOARD = "Скрыть / Раскрыть";
    static final String PRODUCT_UPDATE_NAME_KEYBOARD = "Изменить имя";
    static final String PRODUCT_UPDATE_PRICE_KEYBOARD = "Изменить цену";
    static final String PRODUCT_UPDATE_ICON_KEYBOARD = "Изменить фото";
    static final String PRODUCT_ADD_QUANTITY_KEYBOARD = "Пополнить количество";
    static final String PRODUCT_UPDATE_QUANTITY_KEYBOARD = "Изменить количество";

    static final String USERS_INFO_KEYBOARD = "Общая информация";
    static final String USERS_USER_KEYBOARD = "Пользователь";

    static final String USER_INFO_KEYBOARD = "Информация";
    static final String USER_ORDER_STATUS_KEYBOARD = "Cтатус заказа";
    static final String USER_UPDATE_LOCK_KEYBOARD = "Заблокировать / Разблокировать";

    static final String PICKUP_POINTS_INFO_KEYBOARD = "Общая информация";
    static final String PICKUP_POINTS_ADD_POINT_KEYBOARD = "Добавить";
    static final String PICKUP_POINTS_POINT_KEYBOARD = "Пункт выдачи";

    static final String PICKUP_POINT_INFO_KEYBOARD = "Информация";
    static final String PICKUP_POINT_DELETE_KEYBOARD = "Удалить";
    static final String PICKUP_POINT_UPDATE_LOCATION_KEYBOARD = "Изменить местоположение";

    private static final Set<String> OWNER_RESET_STATE_COMMANDS = new HashSet<>(Arrays.asList(
            ACCESS_RIGHTS_KEYBOARD
    ));

    private static final Set<String> ADMINISTRATOR_RESET_STATE_COMMANDS = new HashSet<>(Arrays.asList(
            MAIN_MENU_KEYBOARD,
            PRODUCTS_KEYBOARD,
            USERS_KEYBOARD,
            PICKUP_POINTS_KEYBOARD,
            STATISTIC_KEYBOARD,
            PRODUCTS_INFO_KEYBOARD,
            PRODUCTS_ADD_PRODUCT_KEYBOARD,
            PRODUCTS_PRODUCT_KEYBOARD,
            PRODUCT_INFO_KEYBOARD,
            PRODUCT_DELETE_KEYBOARD,
            PRODUCT_VISIBILITY_KEYBOARD,
            PRODUCT_UPDATE_NAME_KEYBOARD,
            PRODUCT_UPDATE_PRICE_KEYBOARD,
            PRODUCT_UPDATE_ICON_KEYBOARD,
            PRODUCT_ADD_QUANTITY_KEYBOARD,
            PRODUCT_UPDATE_QUANTITY_KEYBOARD,
            USERS_INFO_KEYBOARD,
            USERS_USER_KEYBOARD,
            USER_INFO_KEYBOARD,
            USER_ORDER_STATUS_KEYBOARD,
            USER_UPDATE_LOCK_KEYBOARD,
            PICKUP_POINTS_INFO_KEYBOARD,
            PICKUP_POINTS_ADD_POINT_KEYBOARD,
            PICKUP_POINTS_POINT_KEYBOARD,
            PICKUP_POINT_INFO_KEYBOARD,
            PICKUP_POINT_DELETE_KEYBOARD,
            PICKUP_POINT_UPDATE_LOCATION_KEYBOARD
    ));

    private static final Set<String> USER_RESET_STATE_COMMANDS = new HashSet<>(Arrays.asList(
            "/start",
            "/registration",
            "/help"
    ));

    private static final Set<String> ADMINISTRATOR_DELETE_MESSAGE_COMMANDS = new HashSet<>(Arrays.asList(
            MAIN_MENU_KEYBOARD,
            PRODUCTS_KEYBOARD,
            USERS_KEYBOARD,
            PICKUP_POINTS_KEYBOARD,
            STATISTIC_KEYBOARD,
            PRODUCTS_PRODUCT_KEYBOARD,
            USERS_USER_KEYBOARD,
            PICKUP_POINTS_POINT_KEYBOARD
    ));

    private static final Set<String> OWNER_DELETE_MESSAGE_COMMANDS = new HashSet<>(Arrays.asList(
            ACCESS_RIGHTS_KEYBOARD
    ));

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
                deleteMessageIfRequired(chatId, message);

                if (text.equals("/start") || text.equals("/registration")) {
                    if (userRepository.existsById(chatId)) {
                        if (text.equals("/start")) {
                            start(chatId);
                        }

                        else {
                            sendMessage(chatId, "Вы уже зарегистрированы");
                        }
                    }

                    else {
                        setState(chatId, ActionType.REGISTRATION);
                        sendMessage(chatId, "Здравствуйте! Вам нужно зарегистрироваться");
                        sendMessage(chatId, "Введите свой российский номер телефона (без пробелов) или электронную почту");
                    }
                }

                else if (text.equals("/help")) {
                    sendMessage(chatId, HELP_TEXT);
                }

                else if (text.equals(MAIN_MENU_KEYBOARD) && checkAccessRights(chatId, AccessRights.ADMINISTRATOR)) {
                    if (checkAccessRights(chatId, AccessRights.OWNER)) {
                        sendOwnerPanel(chatId);
                    }

                    else {
                        sendAdministratorPanel(chatId);
                    }
                }

                else if (text.equals(PRODUCTS_KEYBOARD) && checkAccessRights(chatId, AccessRights.ADMINISTRATOR)) {
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(String.valueOf(chatId));
                    sendMessage.setText("Товары");

                    ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
                    replyKeyboardMarkup.setResizeKeyboard(true);

                    List<KeyboardRow> keyboardRows = new ArrayList<>();

                    keyboardRows.add(createKeyboardRow(PRODUCTS_INFO_KEYBOARD));
                    keyboardRows.add(createKeyboardRow(PRODUCTS_ADD_PRODUCT_KEYBOARD, PRODUCTS_PRODUCT_KEYBOARD));
                    keyboardRows.add(createKeyboardRow(MAIN_MENU_KEYBOARD));

                    replyKeyboardMarkup.setKeyboard(keyboardRows);
                    sendMessage.setReplyMarkup(replyKeyboardMarkup);

                    executeFunction(sendMessage);
                }

                else if (text.equals(USERS_KEYBOARD) && checkAccessRights(chatId, AccessRights.ADMINISTRATOR)) {
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(String.valueOf(chatId));
                    sendMessage.setText("Пользователи");

                    ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
                    replyKeyboardMarkup.setResizeKeyboard(true);

                    List<KeyboardRow> keyboardRows = new ArrayList<>();

                    keyboardRows.add(createKeyboardRow(USERS_INFO_KEYBOARD));
                    keyboardRows.add(createKeyboardRow(USERS_USER_KEYBOARD));
                    keyboardRows.add(createKeyboardRow(MAIN_MENU_KEYBOARD));

                    replyKeyboardMarkup.setKeyboard(keyboardRows);
                    sendMessage.setReplyMarkup(replyKeyboardMarkup);

                    executeFunction(sendMessage);
                }

                else if (text.equals(PICKUP_POINTS_KEYBOARD) && checkAccessRights(chatId, AccessRights.ADMINISTRATOR)) {
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(String.valueOf(chatId));
                    sendMessage.setText("Пункты выдачи");

                    ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
                    replyKeyboardMarkup.setResizeKeyboard(true);

                    List<KeyboardRow> keyboardRows = new ArrayList<>();

                    keyboardRows.add(createKeyboardRow(PICKUP_POINTS_INFO_KEYBOARD));
                    keyboardRows.add(createKeyboardRow(PICKUP_POINTS_ADD_POINT_KEYBOARD, PICKUP_POINTS_POINT_KEYBOARD));
                    keyboardRows.add(createKeyboardRow(MAIN_MENU_KEYBOARD));

                    replyKeyboardMarkup.setKeyboard(keyboardRows);
                    sendMessage.setReplyMarkup(replyKeyboardMarkup);

                    executeFunction(sendMessage);
                }

                else if (text.equals(STATISTIC_KEYBOARD) && checkAccessRights(chatId, AccessRights.ADMINISTRATOR)) {
                    String sendText = "";

                    if (productsRepository.existsById(1)) {
                        sendText += boldAndUnderline("ТОВАРЫ") + "\n\n";

                        sendText += "В ассортименте: " + productsRepository.count() + " товаров \n\n";
                        sendText += boldAndItalic("Сегодня, неделя, месяц") + "\n";
                        sendText += "Количество продаж: " + "\n";
                        sendText += "Выручка: " + "\n\n";
                    }

                    sendText += boldAndUnderline("ПОЛЬЗОВАТЕЛИ") + "\n\n";

                    sendText += "Зарегестрировано: " + userRepository.count() + " человек \n\n";

                    sendText += boldAndItalic("Сегодня, неделя, месяц") + "\n";
                    sendText += "Зарегестрировано: " + "\n";
                    sendText += "Количество заказов: " + "\n";
                    sendText += "Количество покупателей: " + "\n";

                    sendMessage(chatId, sendText);
                }

                else if (text.equals(ACCESS_RIGHTS_KEYBOARD) && checkAccessRights(chatId, AccessRights.OWNER)) {
                    setState(chatId, ActionType.ACCESS_RIGHTS_GIVE);
                    sendMessage(chatId, "Шаг 1. Введите ChatId или Username пользователя");
                }

                else if (text.equals(PRODUCTS_INFO_KEYBOARD) && checkAccessRights(chatId, AccessRights.ADMINISTRATOR)) {
                    String sendText = "";

                    if (productsRepository.existsById(1)) {
                        var products = productsRepository.findAll();
                        productsRepository.count();

                        sendText += boldAndUnderline("ТОВАРЫ") + "\n\n";

                        sendText += "В ассортименте: " + productsRepository.count() + " товаров \n\n";

                        sendText += boldAndItalic("Сегодня, неделя, месяц") + "\n";
                        sendText += "Количество продаж: " + "\n";
                        sendText += "Выручка: " + "\n\n";

                        for (Product product : products) {
                            sendText += product.getId() + ". " + product.getName() + " - " + product.getQuantity() + " шт, " + product.getPrice() + " ₽ \n";
                        }

                        sendMessage(chatId, sendText);
                    }

                    else {
                        sendMessage(chatId, "Товары отсутствуют");
                    }
                }

                else if (text.equals(PRODUCTS_ADD_PRODUCT_KEYBOARD) && checkAccessRights(chatId, AccessRights.ADMINISTRATOR)) {
                    setState(chatId, ActionType.PRODUCT_ADD);
                    sendMessage(chatId, "Шаг 1. Введите имя товара");
                }

                else if (text.equals(PRODUCTS_PRODUCT_KEYBOARD) && checkAccessRights(chatId, AccessRights.ADMINISTRATOR)) {
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(String.valueOf(chatId));
                    sendMessage.setText("Товар");

                    ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
                    replyKeyboardMarkup.setResizeKeyboard(true);

                    List<KeyboardRow> keyboardRows = new ArrayList<>();

                    keyboardRows.add(createKeyboardRow(PRODUCT_INFO_KEYBOARD));
                    keyboardRows.add(createKeyboardRow(PRODUCT_DELETE_KEYBOARD, PRODUCT_VISIBILITY_KEYBOARD));
                    keyboardRows.add(createKeyboardRow(PRODUCT_UPDATE_NAME_KEYBOARD, PRODUCT_UPDATE_PRICE_KEYBOARD, PRODUCT_UPDATE_ICON_KEYBOARD));
                    keyboardRows.add(createKeyboardRow(PRODUCT_ADD_QUANTITY_KEYBOARD, PRODUCT_UPDATE_QUANTITY_KEYBOARD));
                    keyboardRows.add(createKeyboardRow(PRODUCTS_KEYBOARD));

                    replyKeyboardMarkup.setKeyboard(keyboardRows);
                    sendMessage.setReplyMarkup(replyKeyboardMarkup);

                    executeFunction(sendMessage);
                }

                else if (text.equals(PRODUCT_VISIBILITY_KEYBOARD) && checkAccessRights(chatId, AccessRights.ADMINISTRATOR)) {

                }

                else if (text.equals(PRODUCT_DELETE_KEYBOARD) && checkAccessRights(chatId, AccessRights.ADMINISTRATOR)) {
                    setState(chatId, ActionType.PRODUCT_DELETE);

                    sendProductsIdList(chatId);
                }

                else if (text.equals(PRODUCT_VISIBILITY_KEYBOARD) && checkAccessRights(chatId, AccessRights.ADMINISTRATOR)) {
                    setState(chatId, ActionType.PRODUCT_VISIBILITY);

                    sendProductsIdList(chatId);
                }

                else if (text.equals(PRODUCT_UPDATE_NAME_KEYBOARD) && checkAccessRights(chatId, AccessRights.ADMINISTRATOR)) {

                }

                else if (text.equals(PRODUCT_UPDATE_PRICE_KEYBOARD) && checkAccessRights(chatId, AccessRights.ADMINISTRATOR)) {

                }

                else if (text.equals(PRODUCT_UPDATE_ICON_KEYBOARD) && checkAccessRights(chatId, AccessRights.ADMINISTRATOR)) {

                }

                else if (text.equals(PRODUCT_ADD_QUANTITY_KEYBOARD) && checkAccessRights(chatId, AccessRights.ADMINISTRATOR)) {
                    setState(chatId, ActionType.PRODUCT_ADD_QUANTITY);

                    sendProductsIdList(chatId);
                }

                else if (text.equals(PRODUCT_UPDATE_QUANTITY_KEYBOARD) && checkAccessRights(chatId, AccessRights.ADMINISTRATOR)) {
                    setState(chatId, ActionType.PRODUCT_UPDATE_QUANTITY);

                    sendProductsIdList(chatId);
                }

                else if (text.equals(USERS_INFO_KEYBOARD) && checkAccessRights(chatId, AccessRights.ADMINISTRATOR)) {
                    var users = userRepository.findAll();

                    String sendText = "";

                    sendText += boldAndUnderline("ПОЛЬЗОВАТЕЛИ") + "\n\n";

                    sendText += "Зарегестрировано: " + userRepository.count() + " человек \n\n";
                    sendText += boldAndItalic("Сегодня, неделя, месяц") + "\n";
                    sendText += "Зарегестрировано: " + "\n";
                    sendText += "Количество заказов: " + "\n";
                    sendText += "Количество покупателей: " + "\n";

                    int i = 0;

                    for (User user : users) {
                        i += 1;
                        sendText += i + ".  " + user.getChatId() + "\n";
                    }

                    sendMessage(chatId, sendText);
                }

                else if (text.equals(USERS_USER_KEYBOARD) && checkAccessRights(chatId, AccessRights.ADMINISTRATOR)) {
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(String.valueOf(chatId));
                    sendMessage.setText("Пользователь");

                    ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
                    replyKeyboardMarkup.setResizeKeyboard(true);

                    List<KeyboardRow> keyboardRows = new ArrayList<>();

                    keyboardRows.add(createKeyboardRow(USER_INFO_KEYBOARD));
                    keyboardRows.add(createKeyboardRow(USER_ORDER_STATUS_KEYBOARD, USER_UPDATE_LOCK_KEYBOARD));
                    keyboardRows.add(createKeyboardRow(USERS_KEYBOARD));

                    replyKeyboardMarkup.setKeyboard(keyboardRows);
                    sendMessage.setReplyMarkup(replyKeyboardMarkup);

                    executeFunction(sendMessage);
                }

                else if (text.equals(USER_INFO_KEYBOARD) && checkAccessRights(chatId, AccessRights.ADMINISTRATOR)) {

                }

                else if (text.equals(USER_ORDER_STATUS_KEYBOARD) && checkAccessRights(chatId, AccessRights.ADMINISTRATOR)) {

                }

                else if (text.equals(USER_UPDATE_LOCK_KEYBOARD) && checkAccessRights(chatId, AccessRights.ADMINISTRATOR)) {

                }

                else if (text.equals(PICKUP_POINTS_INFO_KEYBOARD) && checkAccessRights(chatId, AccessRights.ADMINISTRATOR)) {

                }

                else if (text.equals(PICKUP_POINTS_ADD_POINT_KEYBOARD) && checkAccessRights(chatId, AccessRights.ADMINISTRATOR)) {

                }

                else if (text.equals(PICKUP_POINTS_POINT_KEYBOARD) && checkAccessRights(chatId, AccessRights.ADMINISTRATOR)) {
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(String.valueOf(chatId));
                    sendMessage.setText("Пункт выдачи");

                    ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
                    replyKeyboardMarkup.setResizeKeyboard(true);

                    List<KeyboardRow> keyboardRows = new ArrayList<>();

                    keyboardRows.add(createKeyboardRow(PICKUP_POINT_INFO_KEYBOARD));
                    keyboardRows.add(createKeyboardRow(PICKUP_POINT_DELETE_KEYBOARD, PICKUP_POINT_UPDATE_LOCATION_KEYBOARD));
                    keyboardRows.add(createKeyboardRow(PICKUP_POINTS_KEYBOARD));

                    replyKeyboardMarkup.setKeyboard(keyboardRows);
                    sendMessage.setReplyMarkup(replyKeyboardMarkup);

                    executeFunction(sendMessage);
                }

                else if (text.equals(PICKUP_POINT_INFO_KEYBOARD) && checkAccessRights(chatId, AccessRights.ADMINISTRATOR)) {

                }

                else if (text.equals(PICKUP_POINT_DELETE_KEYBOARD) && checkAccessRights(chatId, AccessRights.ADMINISTRATOR)) {

                }

                else if (text.equals(PICKUP_POINT_UPDATE_LOCATION_KEYBOARD) && checkAccessRights(chatId, AccessRights.ADMINISTRATOR)) {

                }

                else if (differentStatesRepository.existsById(chatId)) {
                    if (differentStatesRepository.findById(chatId).get().getState() == ActionType.REGISTRATION.getCode()) {
                        registration(message, text);
                    }

                    else if (differentStatesRepository.findById(chatId).get().getState() == ActionType.PRODUCT_ADD.getCode()) {
                        try {
                            addingProduct(message);
                        } catch (TelegramApiException | MalformedURLException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    else if (differentStatesRepository.findById(chatId).get().getState() == ActionType.PRODUCT_DELETE.getCode()) {
                        deleteProductDatabase(chatId, Integer.parseInt(text));
                    }

                    else if (differentStatesRepository.findById(chatId).get().getState() == ActionType.PRODUCT_VISIBILITY.getCode()) {
                        updateVisibilityProductDatabase(chatId, Integer.parseInt(text));
                    }

                    else if (differentStatesRepository.findById(chatId).get().getState() == ActionType.PRODUCT_ADD_QUANTITY.getCode()) {
                        addingQuantityProduct(chatId, text);
                    }

                    else if (differentStatesRepository.findById(chatId).get().getState() == ActionType.PRODUCT_UPDATE_QUANTITY.getCode()) {
                        updatingQuantityProduct(chatId, text);
                    }

                    else if (differentStatesRepository.findById(chatId).get().getState() == ActionType.ACCESS_RIGHTS_GIVE.getCode()) {
                        givingAccessRights(chatId, text);
                    }
                }

                else {
                    // Обработка, если ни одно условие не совпало
                }
            }

            else if (message.hasPhoto()) {
                if (differentStatesRepository.findById(chatId).get().getState() == ActionType.PRODUCT_ADD.getCode()) {
                    try {
                        addingProduct(message);
                    } catch (TelegramApiException | MalformedURLException e) {
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
        if (checkAccessRights(chatId, AccessRights.OWNER)) {
            sendOwnerPanel(chatId);
        }

        else if (checkAccessRights(chatId, AccessRights.ADMINISTRATOR)) {
            sendAdministratorPanel(chatId);
        }

        else {
            User user = userRepository.findById(chatId).get();
            sendMessage(chatId, "Здравствуйте, " + user.getFirstName());
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
            addUserDatabase(message, formattedInput);
            sendMessage(chatId, "Регистрация завершена");

            if (checkAccessRights(chatId, AccessRights.OWNER)) {
                sendMessage(chatId, "Добро пожаловать, владелец " + message.getChat().getFirstName());
                sendOwnerPanel(chatId);
            }
        }

        else {
            sendMessage(chatId, "Номер телефона или электронная почта введены неправильно. Проверьте корректность ввода и введите снова");
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
            sendMessage(chatId, "Шаг 2. Введите ID права доступа");
        }

        else {
            if (isNumeric(text)) {
                int id = Integer.parseInt(text);

                if (id >= AccessRights.OWNER.getCode() && id <= AccessRights.PICKUP_POINT.getCode()) {
                    setAccessRights(chatId, differentState.getStep_1(), id);
                }

                else {
                    sendMessage(chatId, "Права доступа с ID " + id + " отстутствует. Попробуйте еще раз");
                }
            }

            else {
                sendMessage(chatId, "Количество должно быть числом. Попробуйте еще раз");
            }
        }
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
            if (isNumeric(text)) {
                int quantity = Integer.parseInt(text);
                differentState.setStep_2(String.valueOf(quantity));
                differentStatesRepository.save(differentState);
                sendMessage(chatId, "Шаг 3. Введите цену товара");
            }

            else {
                sendMessage(chatId, "Количество должно быть числом. Попробуйте еще раз");
            }
        }

        else if (differentState.getStep_3() == null) {
            if (isNumeric(text)) {
                int price = Integer.parseInt(text);
                differentState.setStep_3(String.valueOf(price));
                differentStatesRepository.save(differentState);
                sendMessage(chatId, "Шаг 4. Отправьте иконку товара");
            }

            else {
                sendMessage(chatId, "Цена должна быть числом. Попробуйте еще раз");
            }
        }

        else {;
            addProductDatabase(message, differentState.getStep_1(), differentState.getStep_2(),
                    differentState.getStep_3());
        }
    }

    private void addingQuantityProduct(long chatId, String text) {
        DifferentState differentState = differentStatesRepository.findById(chatId).get();

        if (differentState.getStep_1() == null) {
            if (isNumeric(text)) {
                int id = Integer.parseInt(text);

                if (productsRepository.existsById(id)) {
                    differentState.setStep_1(text);
                    differentStatesRepository.save(differentState);
                    sendMessage(chatId, "Шаг 2. Введите количество товара");
                }

                else {
                    sendMessage(chatId, "Товар с ID " + text + " отстутствует. Попробуйте еще раз");
                }
            }

            else {
                sendMessage(chatId, "ID должно быть числом. Попробуйте еще раз");
            }
        }

        else {
            if (isNumeric(text)) {
                int quantity = Integer.parseInt(text);
                addQuantityProductDatabase(chatId, Integer.parseInt(differentState.getStep_1()), quantity);
            }

            else {
                sendMessage(chatId, "Количество должно быть числом. Попробуйте еще раз");
            }
        }
    }

    private void updatingQuantityProduct(long chatId, String text) {
        DifferentState differentState = differentStatesRepository.findById(chatId).get();

        if (differentState.getStep_1() == null) {
            if (isNumeric(text)) {
                int id = Integer.parseInt(text);

                if (productsRepository.existsById(id)) {
                    differentState.setStep_1(text);
                    differentStatesRepository.save(differentState);
                    sendMessage(chatId, "Шаг 2. Введите количество товара");
                }

                else {
                    sendMessage(chatId, "Товар с ID " + text + " отстутствует. Попробуйте еще раз");
                }
            }

            else {
                sendMessage(chatId, "ID должно быть числом. Попробуйте еще раз");
            }
        }

        else {
            if (isNumeric(text)) {
                int quantity = Integer.parseInt(text);
                updateQuantityProductDatabase(chatId, Integer.parseInt(differentState.getStep_1()), quantity);
            }

            else {
                sendMessage(chatId, "Количество должно быть числом. Попробуйте еще раз");
            }
        }
    }

    private void setState(long chatId, ActionType action) {
        if (!differentStatesRepository.existsById(chatId)) {
            DifferentState differentState = new DifferentState();
            differentState.setChatId(chatId);

            differentState.setState(action.getCode());
            differentStatesRepository.save(differentState);
        }
    }


    private void addUserDatabase(Message message, String userData) {
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

    private void addProductDatabase(Message message, String name, String quantity, String price) throws TelegramApiException, MalformedURLException {
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

    private void deleteProductDatabase(Long chatId, int id) {
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

    private void updateVisibilityProductDatabase(Long chatId, int id) {
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

        else {
            sendMessage(chatId, "Товар с ID " + id + " отстутствует. Попробуйте еще раз");
        }
    }

    private void addQuantityProductDatabase(Long chatId, int id, int quantity) {
        if (productsRepository.existsById(id)) {
            var product = productsRepository.findById(id).get();

            product.setQuantity(quantity);
            productsRepository.save(product);
            sendMessage(chatId, "Количество товара с ID " + id + " увеличено на " + quantity);

            deleteState(chatId);
        }
    }

    private void updateQuantityProductDatabase(Long chatId, int id, int quantity) {
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

    private boolean checkAccessRights(Long chatId, AccessRights action) {
        if (action.getCode() == AccessRights.OWNER.getCode() && config.getOwnerChatId().equals(chatId)) {
            return true;
        }

        else if (action.getCode() == AccessRights.ADMINISTRATOR.getCode() && (userRepository.findById(chatId).get().getAccessRights() == AccessRights.ADMINISTRATOR.getCode() || config.getOwnerChatId().equals(chatId))) {
            return true;
        }

        else if (action.getCode() == AccessRights.PICKUP_POINT.getCode() && (userRepository.findById(chatId).get().getAccessRights() <= AccessRights.PICKUP_POINT.getCode() || config.getOwnerChatId().equals(chatId))) {
            return true;
        }

        return false;
    }

    private void sendOwnerPanel(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));

        message.setText("Главное меню");

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);

        List<KeyboardRow> keyboardRows = new ArrayList<>();

        var row_1 = new KeyboardRow();

        row_1.add(PRODUCTS_KEYBOARD);
        row_1.add(USERS_KEYBOARD);

        keyboardRows.add(row_1);

        var row_2 = new KeyboardRow();

        row_2.add(PICKUP_POINTS_KEYBOARD);
        row_2.add(STATISTIC_KEYBOARD);

        keyboardRows.add(row_2);

        var row_3 = new KeyboardRow();

        row_3.add(ACCESS_RIGHTS_KEYBOARD);

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

        row_1.add(PRODUCTS_KEYBOARD);
        row_1.add(USERS_KEYBOARD);

        keyboardRows.add(row_1);

        var row_2 = new KeyboardRow();

        row_2.add(PICKUP_POINTS_KEYBOARD);
        row_2.add(STATISTIC_KEYBOARD);

        keyboardRows.add(row_2);

        keyboardMarkup.setKeyboard(keyboardRows);
        message.setReplyMarkup(keyboardMarkup);

        executeFunction(message);
    }

    private KeyboardRow createKeyboardRow(String... buttons) {
        KeyboardRow row = new KeyboardRow();
        row.addAll(Arrays.asList(buttons));
        return row;
    }

    private void deleteState(Long chatId) {
        if (differentStatesRepository.existsById(chatId)) {
            differentStatesRepository.deleteById(chatId);
        }
    }

    private void resetState(long chatId, String text) {
        if (userRepository.existsById(chatId)) {
            if (shouldResetState(chatId, text)) {
                deleteState(chatId);
            }
        }
    }

    private boolean shouldResetState(long chatId, String text) {
        if (checkAccessRights(chatId, AccessRights.ADMINISTRATOR) && ADMINISTRATOR_RESET_STATE_COMMANDS.contains(text)) {
            return true;
        }

        if (checkAccessRights(chatId, AccessRights.OWNER) && OWNER_RESET_STATE_COMMANDS.contains(text)) {
            return true;
        }

        return USER_RESET_STATE_COMMANDS.contains(text);
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

    private void sendMessage(long chatId, Object text) {
        SendMessage message = new SendMessage();
        message.enableHtml(true);
        message.setChatId(String.valueOf(chatId));
        message.setText(text.toString());

        executeFunction(message);
    }

    private void deleteMessage(long chatId, int messageId) {
        DeleteMessage message = new DeleteMessage();
        message.setChatId(String.valueOf(chatId));
        message.setMessageId(messageId);

        executeFunction(message);
    }

    private void deleteMessageIfRequired(long chatId, Message message) {
        if (shouldDeleteMessage(chatId, message.getText())) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    deleteMessage(chatId, message.getMessageId());
                }
            }, 300);
        }
    }

    private boolean shouldDeleteMessage(long chatId, String text) {
        if (checkAccessRights(chatId, AccessRights.ADMINISTRATOR) && ADMINISTRATOR_DELETE_MESSAGE_COMMANDS.contains(text)) {
            return true;
        }

        return checkAccessRights(chatId, AccessRights.OWNER) && OWNER_DELETE_MESSAGE_COMMANDS.contains(text);
    }

    private void executeFunction(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error: " + e.getMessage());
        }
    }

    private void executeFunction(DeleteMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error: " + e.getMessage());
        }
    }

    private boolean isValidPhoneNumber(String input) {
        return input.matches("^(\\+7|8)?\\d{9}$");
    }

    private boolean isValidEmail(String input) {
        return input.matches("^[\\w-.]+@[\\w-]+\\.[a-z]{2,}$");
    }

    private String formatPhoneNumber(String input) {
        if (input.startsWith("8")) {
            input = "+7" + input.substring(1);
        } else if (!input.startsWith("+7")) {
            input = "+7" + input;
        }
        return input;
    }

    public static String removeAtSymbol(String text) {
        return text.replace("@", "");
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

    private static String bold(String text) {
        return "<b>%s</b>".formatted(text);
    }

    private static String italic(String text) {
        return "<i>%s</i>".formatted(text);
    }

    private static String boldAndItalic(String text) {
        return "<b><i>%s</i></b>".formatted(text);
    }

    private static String boldAndUnderline(String text) {
        return "<b><u>%s</u></b>".formatted(text);
    }



    private void sendProductsIdList(Long chatId) {
        String sendText = "";

        if (productsRepository.existsById(1)) {
            var products = productsRepository.findAll();
            sendText += boldAndUnderline("ТОВАРЫ") + "\n\n";

            for (Product product : products) {
                sendText += product.getId() + ". " + product.getName() + "\n";
            }

            sendMessage(chatId, sendText);
            sendMessage(chatId, "Введите ID товара");
        }

        else {
            sendMessage(chatId, "Товары отсутствуют");
        }
    }
}