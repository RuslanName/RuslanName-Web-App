package mainFiles.services;

import mainFiles.configs.BotConfig;

import mainFiles.database.tables.pickupPoint.PickupPoint;
import mainFiles.database.tables.pickupPoint.PickupPointsRepository;
import mainFiles.database.tables.user.User;
import mainFiles.database.tables.user.UsersRepository;
import mainFiles.database.tables.differentState.DifferentState;
import mainFiles.database.tables.differentState.DifferentStatesRepository;
import mainFiles.database.tables.product.ProductsRepository;
import mainFiles.database.tables.product.Product;
import mainFiles.database.tables.userCart.UserCartsRepository;
import mainFiles.database.tables.userOrder.UserOrdersRepository;
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
    private UsersRepository usersRepository;

    @Autowired
    private DifferentStatesRepository differentStatesRepository;

    @Autowired
    private ProductsRepository productsRepository;

    @Autowired
    private PickupPointsRepository pickupPointsRepository;

    @Autowired
    private UserCartsRepository userCartsRepository;

    @Autowired
    private UserOrdersRepository userOrdersHistoryRepository;

    @Autowired
    private DatabaseService databaseService;

    final BotConfig config;

    static final String MAIN_MENU_KEYBOARD = "Главное меню";

    static final String PRODUCTS_KEYBOARD = "Товары";
    static final String USERS_KEYBOARD = "Пользователи";
    static final String PICKUP_POINTS_KEYBOARD = "Пункты выдачи";
    static final String STATISTIC_KEYBOARD = "Статистика";
    static final String ACCESS_RIGHTS_KEYBOARD = "Дать права доступа";

    static final String PRODUCTS_INFO_KEYBOARD = "Общая информация товаров";
    static final String PRODUCTS_ADD_PRODUCT_KEYBOARD = "Добавить товар";
    static final String PRODUCTS_PRODUCT_KEYBOARD = "Товар";

    static final String PRODUCT_INFO_KEYBOARD = "Информация товара";
    static final String PRODUCT_DELETE_KEYBOARD = "Удалить товар";
    static final String PRODUCT_VISIBILITY_KEYBOARD = "Скрыть / Раскрыть товар";
    static final String PRODUCT_UPDATE_NAME_KEYBOARD = "Изменить имя товара";
    static final String PRODUCT_UPDATE_PRICE_KEYBOARD = "Изменить цену товара";
    static final String PRODUCT_UPDATE_ICON_KEYBOARD = "Изменить фото товара";
    static final String PRODUCT_ADD_QUANTITY_KEYBOARD = "Пополнить количество товара";
    static final String PRODUCT_UPDATE_QUANTITY_KEYBOARD = "Изменить количество товара";

    static final String USERS_INFO_KEYBOARD = "Общая информация пользователей";
    static final String USERS_USER_KEYBOARD = "Пользователь";

    static final String USER_INFO_KEYBOARD = "Информация о пользователе";
    static final String USER_ORDER_STATUS_KEYBOARD = "Cтатус заказа пользователя";
    static final String USER_UPDATE_BLOCK_KEYBOARD = "Заблокировать / Разблокировать пользователя";

    static final String PICKUP_POINTS_INFO_KEYBOARD = "Общая информация пунктов выдачи";
    static final String PICKUP_POINTS_ADD_POINT_KEYBOARD = "Добавить пункт выдачи";
    static final String PICKUP_POINTS_POINT_KEYBOARD = "Пункт выдачи";

    static final String PICKUP_POINT_INFO_KEYBOARD = "Информация пункта выдачи";
    static final String PICKUP_POINT_DELETE_KEYBOARD = "Удалить пункт выдачи";
    static final String PICKUP_POINT_UPDATE_LOCATION_KEYBOARD = "Изменить местоположение пункта выдачи";

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
            PRODUCT_UPDATE_QUANTITY_KEYBOARD,
            USERS_INFO_KEYBOARD,
            USERS_USER_KEYBOARD,
            PICKUP_POINTS_INFO_KEYBOARD,
            PICKUP_POINTS_ADD_POINT_KEYBOARD,
            PICKUP_POINTS_POINT_KEYBOARD
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

            if (usersRepository.findById(chatId)
                    .map(user -> user.isBlock())
                    .orElse(false)) {
                sendMessage(chatId, "Ваш аккаунт заблокирован");
            }

            else if (message.hasText()) {
                String text = message.getText();

                if (!(text.equals("/start") || text.equals("/registration") ||
                        differentStatesRepository.findById(chatId)
                                .map(state -> state.getState() == ActionType.REGISTRATION.getCode())
                                .orElse(false))) {
                    resetState(chatId, text);
                    deleteMessageIfRequired(chatId, message);
                }

                if (text.equals("/start") || text.equals("/registration")) {
                    if (usersRepository.existsById(chatId)) {
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
                    sendProductsPanel(chatId);
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
                    sendPickupPointsPanel(chatId);
                }

                else if (text.equals(STATISTIC_KEYBOARD) && checkAccessRights(chatId, AccessRights.ADMINISTRATOR)) {
                    String sendText = "";

                    if (productsRepository.existsById(1)) {
                        sendText += boldAndUnderline("ТОВАРЫ") + "\n\n";

                        sendText += "Зарегестрировано: " + productsRepository.count() + " товаров \n\n";

                        sendText += boldAndItalic("Сегодня, неделя, месяц") + "\n";
                        sendText += "Количество продаж: " + "\n";
                        sendText += "Выручка: " + "\n\n";
                    }

                    sendText += boldAndUnderline("ПОЛЬЗОВАТЕЛИ") + "\n\n";

                    sendText += "Зарегестрировано: " + usersRepository.count() + " человек \n\n";

                    sendText += boldAndItalic("Сегодня, неделя, месяц") + "\n";
                    sendText += "Зарегестрировано: " + "\n";
                    sendText += "Количество заказов: " + "\n";
                    sendText += "Количество покупателей: " + "\n\n";

                    if (pickupPointsRepository.existsById(1)) {
                        sendText += boldAndUnderline("ТОВАРЫ") + "\n\n";

                        sendText += "Зарегестрировано: " + productsRepository.count() + " товаров \n\n";

                        sendText += boldAndItalic("Сегодня, неделя, месяц") + "\n";
                        sendText += "Количество полученных заказов: " + "\n";
                        sendText += "Количество отданных заказов: " + "\n";
                        sendText += "Количество покупателей: " + "\n";
                    }

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

                        sendText += boldAndUnderline("ТОВАРЫ") + "\n\n";

                        sendText += "Зарегестрировано: " + productsRepository.count() + " штук \n\n";

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
                    setState(chatId, ActionType.PRODUCT);

                    sendProductsIdList(chatId);
                }

                else if (text.equals(PRODUCT_INFO_KEYBOARD) && checkAccessRights(chatId, AccessRights.ADMINISTRATOR)) {
                    String sendText = "";

                    Product product = productsRepository.findById(Integer.valueOf(differentStatesRepository.findById(chatId).get().getStep_1())).get();

                    sendText += boldAndUnderline(product.getName().toUpperCase()) + "\n\n";

                    sendText += boldAndItalic("Информация") + "\n";
                    sendText += "Цена: " + product.getPrice() + "\n";
                    sendText += "Количество: " + product.getQuantity() + "\n";
                    sendText += "Видимость: " + (product.isVisibility() ? "включена" : "отключена");

                    sendMessage(chatId, sendText);
                }

                else if (text.equals(PRODUCT_DELETE_KEYBOARD) && checkAccessRights(chatId, AccessRights.ADMINISTRATOR)) {
                    try {
                        deleteProductDatabase(chatId);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    sendProductsPanel(chatId);
                }

                else if (text.equals(PRODUCT_VISIBILITY_KEYBOARD) && checkAccessRights(chatId, AccessRights.ADMINISTRATOR)) {
                    updateVisibilityProductDatabase(chatId);
                }

                else if (text.equals(PRODUCT_UPDATE_NAME_KEYBOARD) && checkAccessRights(chatId, AccessRights.ADMINISTRATOR)) {
                    setState(chatId, ActionType.PRODUCT_UPDATE_NAME);
                    sendMessage(chatId, "Введите имя товара");
                }

                else if (text.equals(PRODUCT_UPDATE_PRICE_KEYBOARD) && checkAccessRights(chatId, AccessRights.ADMINISTRATOR)) {
                    setState(chatId, ActionType.PRODUCT_UPDATE_PRICE);
                    sendMessage(chatId, "Введите цену товара");
                }

                else if (text.equals(PRODUCT_UPDATE_ICON_KEYBOARD) && checkAccessRights(chatId, AccessRights.ADMINISTRATOR)) {
                    setState(chatId, ActionType.PRODUCT_UPDATE_ICON);
                    sendMessage(chatId, "Отправьте изображение товара");
                }

                else if (text.equals(PRODUCT_ADD_QUANTITY_KEYBOARD) && checkAccessRights(chatId, AccessRights.ADMINISTRATOR)) {
                    setState(chatId, ActionType.PRODUCT_ADD_QUANTITY);
                    sendMessage(chatId, "Введите количество товара");
                }

                else if (text.equals(PRODUCT_UPDATE_QUANTITY_KEYBOARD) && checkAccessRights(chatId, AccessRights.ADMINISTRATOR)) {
                    setState(chatId, ActionType.PRODUCT_UPDATE_QUANTITY);
                    sendMessage(chatId, "Введите количество товара");
                }

                else if (text.equals(USERS_INFO_KEYBOARD) && checkAccessRights(chatId, AccessRights.ADMINISTRATOR)) {
                    var users = usersRepository.findAll();

                    String sendText = "";

                    sendText += boldAndUnderline("ПОЛЬЗОВАТЕЛИ") + "\n\n";

                    sendText += "Зарегестрировано: " + usersRepository.count() + " человек \n\n";

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
                    setState(chatId, ActionType.USER);
                    sendMessage(chatId, "Введите ChatId или Username пользователя");
                }

                else if (text.equals(USER_INFO_KEYBOARD) && checkAccessRights(chatId, AccessRights.ADMINISTRATOR)) {
                    String sendText = "";

                    User user = findUserById(chatId);

                    sendText += boldAndUnderline(user.getUserName().toUpperCase()) + "\n\n";

                    sendText += boldAndItalic("Информация") + "\n";
                    sendText += "ChatID: " + user.getChatId() + "\n";
                    sendText += "Имя и Фамилия: " + user.getFirstName() + " " + user.getLastName() + "\n";
                    sendText += "Права доступа: "  + user.getAccessRights() + "\n";
                    sendText += "Дата регистрации: "  + user.getRegisteredAt() + "\n";
                    sendText += "Блокировка: " + (user.isBlock() ? "есть" : "нет");

                    sendMessage(chatId, sendText);
                }

                else if (text.equals(USER_ORDER_STATUS_KEYBOARD) && checkAccessRights(chatId, AccessRights.ADMINISTRATOR)) {
                    // НЕ ДОДЕЛАНО!!!
                }

                else if (text.equals(USER_UPDATE_BLOCK_KEYBOARD) && checkAccessRights(chatId, AccessRights.ADMINISTRATOR)) {
                    updateBlockUserDatabase(chatId);
                }

                else if (text.equals(PICKUP_POINTS_INFO_KEYBOARD) && checkAccessRights(chatId, AccessRights.ADMINISTRATOR)) {
                    var pickupPoints = pickupPointsRepository.findAll();

                    String sendText = "";

                    sendText += boldAndUnderline("ПУНКТЫ ВЫДАЧИ") + "\n\n";

                    sendText += "Зарегестрировано: " + pickupPointsRepository.count() + " штук \n\n";

                    sendText += boldAndItalic("Сегодня, неделя, месяц") + "\n";
                    sendText += "Количество полученных заказов: " + "\n";
                    sendText += "Количество отданных заказов: " + "\n";
                    sendText += "Количество покупателей: " + "\n\n";

                    for (PickupPoint pickupPoint : pickupPoints) {
                        sendText += pickupPoint.getId() + ".  " + pickupPoint.getName() + "\n";
                    }

                    sendMessage(chatId, sendText);
                }

                else if (text.equals(PICKUP_POINTS_ADD_POINT_KEYBOARD) && checkAccessRights(chatId, AccessRights.ADMINISTRATOR)) {
                    setState(chatId, ActionType.PICKUP_POINT_ADD);
                    sendMessage(chatId, "Шаг 1. Введите имя пункта выдачи");
                }

                else if (text.equals(PICKUP_POINTS_POINT_KEYBOARD) && checkAccessRights(chatId, AccessRights.ADMINISTRATOR)) {
                    setState(chatId, ActionType.PICKUP_POINT);

                    sendPickupPointsIdList(chatId);
                }

                else if (text.equals(PICKUP_POINT_INFO_KEYBOARD) && checkAccessRights(chatId, AccessRights.ADMINISTRATOR)) {
                    String sendText = "";

                    var pickupPoint = findUserById(chatId);

                    sendText += boldAndUnderline(pickupPoint.getUserName().toUpperCase()) + "\n\n";

                    sendText += boldAndItalic("Информация") + "\n";
                    sendText += "Дата регистрации: " + pickupPoint.getRegisteredAt() + "\n";

                    sendMessage(chatId, sendText);
                }

                else if (text.equals(PICKUP_POINT_DELETE_KEYBOARD) && checkAccessRights(chatId, AccessRights.ADMINISTRATOR)) {
                    deletePickupPointDatabase(chatId);

                    sendPickupPointsPanel(chatId);
                }

                else if (text.equals(PICKUP_POINT_UPDATE_LOCATION_KEYBOARD) && checkAccessRights(chatId, AccessRights.ADMINISTRATOR)) {
                    setState(chatId, ActionType.PICKUP_POINT_UPDATE_LOCATION);
                    sendMessage(chatId, "Введите локацию пункта выдачи");
                }

                else if (differentStatesRepository.existsById(chatId)) {
                    if (differentStatesRepository.findById(chatId).get().getState() == ActionType.REGISTRATION.getCode()) {
                        registration(message, text);
                    }

                    else if (differentStatesRepository.findById(chatId).get().getState() == ActionType.ACCESS_RIGHTS_GIVE.getCode()) {
                        givingAccessRights(chatId, text);
                    }

                    else if (differentStatesRepository.findById(chatId).get().getState() == ActionType.PRODUCT.getCode()) {
                        if (checkProductId(chatId, text)) {
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
                    }

                    else if (differentStatesRepository.findById(chatId).get().getState() == ActionType.PRODUCT_ADD.getCode()) {
                        try {
                            addingProduct(message);
                        } catch (TelegramApiException | MalformedURLException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    else if (differentStatesRepository.findById(chatId).get().getState() == ActionType.PRODUCT_UPDATE_NAME.getCode()) {
                        updateNameProductDatabase(chatId, text);
                    }

                    else if (differentStatesRepository.findById(chatId).get().getState() == ActionType.PRODUCT_UPDATE_PRICE.getCode()) {
                        updatingPriceProduct(chatId, text);
                    }

                    else if (differentStatesRepository.findById(chatId).get().getState() == ActionType.PRODUCT_ADD_QUANTITY.getCode()) {
                        addingQuantityProduct(chatId, text);
                    }

                    else if (differentStatesRepository.findById(chatId).get().getState() == ActionType.PRODUCT_UPDATE_QUANTITY.getCode()) {
                        updatingQuantityProduct(chatId, text);
                    }

                    else if (differentStatesRepository.findById(chatId).get().getState() == ActionType.USER.getCode()) {
                        if (checkUserId(chatId, text)) {
                            SendMessage sendMessage = new SendMessage();
                            sendMessage.setChatId(String.valueOf(chatId));
                            sendMessage.setText("Пользователь");

                            ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
                            replyKeyboardMarkup.setResizeKeyboard(true);

                            List<KeyboardRow> keyboardRows = new ArrayList<>();

                            keyboardRows.add(createKeyboardRow(USER_INFO_KEYBOARD));

                            User user = findUserById(chatId);

                            if ((checkAccessRights(chatId, AccessRights.OWNER) && !checkAccessRights(user.getChatId(), AccessRights.OWNER)) ||
                                    (checkAccessRights(chatId, AccessRights.ADMINISTRATOR) && !checkAccessRights(user.getChatId(), AccessRights.ADMINISTRATOR))) {
                                keyboardRows.add(createKeyboardRow(USER_ORDER_STATUS_KEYBOARD, USER_UPDATE_BLOCK_KEYBOARD));
                            }

                            else {
                                keyboardRows.add(createKeyboardRow(USER_ORDER_STATUS_KEYBOARD));
                            }

                            keyboardRows.add(createKeyboardRow(USERS_KEYBOARD));

                            replyKeyboardMarkup.setKeyboard(keyboardRows);
                            sendMessage.setReplyMarkup(replyKeyboardMarkup);

                            executeFunction(sendMessage);
                        }
                    }

                    else if (differentStatesRepository.findById(chatId).get().getState() == ActionType.PICKUP_POINT.getCode()) {
                        if (checkPickupPointId(chatId, text)) {
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
                    }
                }

                else if (differentStatesRepository.findById(chatId).get().getState() == ActionType.PICKUP_POINT_ADD.getCode()) {
                    addPickupPointDatabase(message);
                }

                else if (differentStatesRepository.findById(chatId).get().getState() == ActionType.PICKUP_POINT_UPDATE_LOCATION.getCode()) {
                    updateLocationPickupPointDatabase(chatId, text);
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

                else if (differentStatesRepository.findById(chatId).get().getState() == ActionType.PRODUCT_UPDATE_ICON.getCode()) {
                    try {
                        updateIconProductDatabase(message);
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
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
            User user = usersRepository.findById(chatId).get();
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

            if (isNumeric(text) && usersRepository.existsById(Long.valueOf(text))) {
                differentState.setStep_1(text);
            }

            else if (usersRepository.existsByUserName(formattedText)) {
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

    private void updatingPriceProduct(long chatId, String text) {
        DifferentState differentState = differentStatesRepository.findById(chatId).get();

        if (isNumeric(text)) {
            int quantity = Integer.parseInt(text);
            updatePriceProductDatabase(chatId, quantity);
        }

        else {
            sendMessage(chatId, "Количество должно быть числом. Попробуйте еще раз");
        }
    }

    private void addingQuantityProduct(long chatId, String text) {
        DifferentState differentState = differentStatesRepository.findById(chatId).get();

        if (isNumeric(text)) {
            int quantity = Integer.parseInt(text);
            addQuantityProductDatabase(chatId, quantity);
        }

        else {
            sendMessage(chatId, "Количество должно быть числом. Попробуйте еще раз");
        }
    }

    private void updatingQuantityProduct(long chatId, String text) {
        DifferentState differentState = differentStatesRepository.findById(chatId).get();

        if (isNumeric(text)) {
            int quantity = Integer.parseInt(text);
            updateQuantityProductDatabase(chatId, quantity);
        }

        else {
            sendMessage(chatId, "Количество должно быть числом. Попробуйте еще раз");
        }
    }

    private void setState(long chatId, ActionType action) {
        if (!differentStatesRepository.existsById(chatId)) {
            DifferentState differentState = new DifferentState();
            differentState.setChatId(chatId);

            differentState.setState(action.getCode());
            differentStatesRepository.save(differentState);
        }

        else {
            DifferentState differentState = differentStatesRepository.findById(chatId).get();
            differentState.setState(action.getCode());
            differentStatesRepository.save(differentState);
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
        product.setRegisteredAt(new Timestamp(System.currentTimeMillis()));

        productsRepository.save(product);

        sendMessage(chatId, "Товар добавлен");

        deleteState(chatId);
    }

    private void deleteProductDatabase(Long chatId) throws IOException {
        int id = Integer.parseInt(differentStatesRepository.findById(chatId).get().getStep_1());
        Product product = productsRepository.findById(id).get();

        String iconPathToDelete = product.getIconPath();
        productsRepository.deleteById(id);

        Files.deleteIfExists(Path.of(iconPathToDelete));

        databaseService.updateDatabaseSequences("products_data");

        sendMessage(chatId, "Товар c ID " + id + " удалён");

        deleteState(chatId);
    }

    private void updateVisibilityProductDatabase(Long chatId) {
        int id = Integer.parseInt(differentStatesRepository.findById(chatId).get().getStep_1());
        Product product = productsRepository.findById(id).get();

        if (product.isVisibility()) {
            product.setVisibility(false);
            productsRepository.save(product);
            sendMessage(chatId, "Товар с ID " + id + " скрыт");
        }

        else {
            product.setVisibility(true);
            productsRepository.save(product);
            sendMessage(chatId, "Товар с ID " + id + " раскрыт");
        }

        resetActionTypeState(chatId, ActionType.PRODUCT);
    }

    private void updateNameProductDatabase(Long chatId, String text) {
        int id = Integer.parseInt(differentStatesRepository.findById(chatId).get().getStep_1());
        Product product = productsRepository.findById(id).get();

        product.setName(text);
        productsRepository.save(product);
        sendMessage(chatId, "Имя товара с ID " + id + " изменено на " + text);

        resetActionTypeState(chatId, ActionType.PRODUCT);
    }

    private void updatePriceProductDatabase(Long chatId, int quantity) {
        int id = Integer.parseInt(differentStatesRepository.findById(chatId).get().getStep_1());
        Product product = productsRepository.findById(id).get();

        product.setPrice(quantity);
        productsRepository.save(product);
        sendMessage(chatId, "Цена товара с ID " + id + " изменена на " + quantity);

        resetActionTypeState(chatId, ActionType.PRODUCT);
    }

    private void updateIconProductDatabase(Message message) throws TelegramApiException, IOException {
        Long chatId = message.getChatId();
        int id = Integer.parseInt(differentStatesRepository.findById(chatId).get().getStep_1());
        Product product = productsRepository.findById(id).get();

        String iconPathToDelete = product.getIconPath();

        String iconPath = config.getProductIconsPath() + saveProductIcon(message);
        product.setIconPath(iconPath);

        Files.deleteIfExists(Path.of(iconPathToDelete));

        productsRepository.save(product);
        sendMessage(chatId, "Изображение товара с ID " + id + " изменено");

        resetActionTypeState(chatId, ActionType.PRODUCT);
    }

    private void addQuantityProductDatabase(Long chatId, int quantity) {
        int id = Integer.parseInt(differentStatesRepository.findById(chatId).get().getStep_1());
        Product product = productsRepository.findById(id).get();

        product.setQuantity(quantity);
        productsRepository.save(product);
        sendMessage(chatId, "Количество товара с ID " + id + " увеличено на " + quantity);

        resetActionTypeState(chatId, ActionType.PRODUCT);
    }

    private void updateQuantityProductDatabase(Long chatId, int quantity) {
        int id = Integer.parseInt(differentStatesRepository.findById(chatId).get().getStep_1());
        Product product = productsRepository.findById(id).get();

        product.setQuantity(quantity);
        productsRepository.save(product);
        sendMessage(chatId, "Количество товара с ID " + id + " изменено на " + quantity);

        resetActionTypeState(chatId, ActionType.PRODUCT);
    }

    private void addUserDatabase(Message message, String userData) {
        var chat = message.getChat();
        long chatId = message.getChatId();

        if (!usersRepository.existsById(chatId)) {
            User user = new User();
            user.setChatId(chatId);
            user.setUserName(chat.getUserName());
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setUserData(userData);
            user.setAccessRights(3);
            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));

            usersRepository.save(user);

            deleteState(chatId);
        }
    }

    private void updateBlockUserDatabase(long chatId) {
        User user = findUserById(chatId);

        if (user.isBlock()) {
            user.setBlock(false);
            usersRepository.save(user);
            sendMessage(chatId, "Пользователь с ID " + user.getChatId() + " разблокирован");
        }

        else {
            user.setBlock(true);
            usersRepository.save(user);
            sendMessage(chatId, "Пользователь с ID " + user.getChatId() + " заблокирован");
        }

        resetActionTypeState(chatId, ActionType.USER);
    }

    private void addPickupPointDatabase(Message message) {
        long chatId = message.getChatId();

        PickupPoint pickupPoint = new PickupPoint();

        pickupPoint.setId(databaseService.getNextId("pickup_points_data_data"));

        pickupPoint.setName(message.getText());
        pickupPoint.setRegisteredAt(new Timestamp(System.currentTimeMillis()));

        pickupPointsRepository.save(pickupPoint);

        sendMessage(chatId, "Пункт выдачи добавлен");

        deleteState(chatId);
    }

    private void deletePickupPointDatabase(long chatId) {
        int id = Integer.parseInt(differentStatesRepository.findById(chatId).get().getStep_1());
        PickupPoint pickupPoint = pickupPointsRepository.findById(id).get();

        productsRepository.deleteById(id);

        databaseService.updateDatabaseSequences("products_data");

        sendMessage(chatId, "Пункт выдачи c ID " + id + " удалён");

        deleteState(chatId);
    }

    private void updateLocationPickupPointDatabase(long chatId, String text) {
        int id = Integer.parseInt(differentStatesRepository.findById(chatId).get().getStep_1());
        PickupPoint pickupPoint = pickupPointsRepository.findById(id).get();

        pickupPoint.setName(text);
        pickupPointsRepository.save(pickupPoint);
        sendMessage(chatId, "Локация пункта выдачи с ID " + id + " изменено на " + text);

        resetActionTypeState(chatId, ActionType.PRODUCT);
    }

    private void setAccessRights(Long chatId, String userIdentification, int id) {
        if (isNumeric(userIdentification)) {
            var user = usersRepository.findById(Long.valueOf(userIdentification)).get();
            user.setAccessRights(id);
            usersRepository.save(user);
            sendMessage(chatId, "Пользователю с ChatId " + userIdentification + " выданы права доступа " + id + " уровня");
        }

        else {
            var user = usersRepository.findByUserName(userIdentification).get();
            user.setAccessRights(id);
            usersRepository.save(user);
            sendMessage(chatId, "Пользователю @" + userIdentification + " выданы права доступа " + id + " уровня");
        }

        deleteState(chatId);
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

    private void sendProductsPanel(long chatId) {
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

    private void sendPickupPointsPanel(long chatId) {
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

    private void sendPickupPointsIdList(Long chatId) {
        String sendText = "";

        if (productsRepository.existsById(1)) {
            var pickupPoints = pickupPointsRepository.findAll();
            sendText += boldAndUnderline("ПУНКТЫ ВЫДАЧИ") + "\n\n";

            for (PickupPoint pickupPoint : pickupPoints) {
                sendText += pickupPoint.getId() + ". " + pickupPoint.getName() + "\n";
            }

            sendMessage(chatId, sendText);
            sendMessage(chatId, "Введите ID пункта выдачи");
        }

        else {
            sendMessage(chatId, "Пункты выдвчи отсутствуют");
        }
    }

    private boolean checkAccessRights(Long chatId, AccessRights action) {
        if (action.getCode() == AccessRights.OWNER.getCode() && config.getOwnerChatId().equals(chatId)) {
            return true;
        }

        else if (action.getCode() == AccessRights.ADMINISTRATOR.getCode() && (usersRepository.findById(chatId).get().getAccessRights() == AccessRights.ADMINISTRATOR.getCode() || config.getOwnerChatId().equals(chatId))) {
            return true;
        }

        else if (action.getCode() == AccessRights.PICKUP_POINT.getCode() && (usersRepository.findById(chatId).get().getAccessRights() <= AccessRights.PICKUP_POINT.getCode() || config.getOwnerChatId().equals(chatId))) {
            return true;
        }

        return false;
    }

    private boolean checkProductId(Long chatId, String text) {
        DifferentState differentState = differentStatesRepository.findById(chatId).get();

        if (differentState.getStep_1() == null) {
            if (isNumeric(text)) {
                int id = Integer.parseInt(text);

                if (productsRepository.existsById(id)) {
                    differentState.setStep_1(text);
                    differentStatesRepository.save(differentState);
                    return true;
                }

                else {
                    sendMessage(chatId, "Товар с ID " + text + " отстутствует. Попробуйте еще раз");
                }
            }

            else {
                sendMessage(chatId, "ID должно быть числом. Попробуйте еще раз");
            }
        }
        return false;
    }

    private boolean checkUserId(Long chatId, String text) {
        DifferentState differentState = differentStatesRepository.findById(chatId).get();

        if (differentState.getStep_1() == null) {
            String formattedText = removeAtSymbol(text);

            if ((isNumeric(text) && usersRepository.existsById(Long.valueOf(text))) || usersRepository.existsByUserName(formattedText)) {
                differentState.setStep_1(formattedText);
                differentStatesRepository.save(differentState);
                return true;
            }

            else {
                sendMessage(chatId, "ChatId или Username пользователя введены неправильно. Проверьте корректность ввода и введите снова");
            }
        }
        return false;
    }

    private boolean checkPickupPointId(Long chatId, String text) {
        DifferentState differentState = differentStatesRepository.findById(chatId).get();

        if (differentState.getStep_1() == null) {
            if (isNumeric(text)) {
                int id = Integer.parseInt(text);

                if (pickupPointsRepository.existsById(id)) {
                    differentState.setStep_1(text);
                    differentStatesRepository.save(differentState);
                    return true;
                }

                else {
                    sendMessage(chatId, "Пункт выдачи с ID " + text + " отстутствует. Попробуйте еще раз");
                }
            }

            else {
                sendMessage(chatId, "ID должно быть числом. Попробуйте еще раз");
            }
        }
        return false;
    }

    private User findUserById(Long chatId) {
        String text = differentStatesRepository.findById(chatId).get().getStep_1();

        if (isNumeric(text)) {
            return usersRepository.findById(Long.valueOf(text)).get();
        }

        else {
            return usersRepository.findByUserName(text).get();
        }
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
        if (usersRepository.existsById(chatId)) {
            if (shouldResetState(chatId, text)) {
                deleteState(chatId);
            }
        }
    }

    private void resetActionTypeState(long chatId, ActionType actionType) {
        if (differentStatesRepository.existsById(chatId)) {
            DifferentState differentState = new DifferentState();

            differentState.setChatId(chatId);
            differentState.setState(actionType.getCode());
            differentState.setStep_1(differentStatesRepository.findById(chatId).get().getStep_1());

            differentStatesRepository.save(differentState);
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
        return text != null && text.matches("-?\\d+");
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
}