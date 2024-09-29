package mainFiles.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import mainFiles.config.BotConfig;
import mainFiles.model.user.User;
import mainFiles.model.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import mainFiles.model.userProducts.UserProducts;
import mainFiles.model.userProducts.UserProductsRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.telegram.telegrambots.meta.api.objects.webapp.WebAppData;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProductsRepository userProductsRepository;

    final BotConfig config;

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
            long chatId = message.getChatId();

            if (message.hasText() && message.getText().equals("/start")) {
                start(chatId);
            }

            if (message.getWebAppData() != null) {
                String webAppData = message.getWebAppData().getData();

                if (webAppData != null) {
                    ObjectMapper objectMapper = new ObjectMapper();
                    try {
                        Map<String, Object> data = objectMapper.readValue(webAppData, Map.class);

                        if (data.containsKey("productId") && data.containsKey("quantity")) {
                            int productId = Integer.parseInt(data.get("productId").toString());
                            int quantity = Integer.parseInt(data.get("quantity").toString());

                            addUserProductsDB(chatId, productId, quantity);
                        }
                    } catch (JsonProcessingException e) {
                        log.error("Error parsing WebApp data", e);
                    }
                }
            }
        }
    }

    private void start(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Откройте приложение");

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);

        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();

        KeyboardButton openAppButton = new KeyboardButton("Приложение");
        openAppButton.setWebApp(new WebAppInfo("https://main--zippy-cheesecake-7a1392.netlify.app"));

        row.add(openAppButton);
        keyboardRows.add(row);

        replyKeyboardMarkup.setKeyboard(keyboardRows);
        message.setReplyMarkup(replyKeyboardMarkup);

        executeFunction(message);
    }

    private void sendShopButton(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Регистрация завершена! Перейдите в магазин");

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);

        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();

        KeyboardButton shopButton = new KeyboardButton("Магазин");
        shopButton.setWebApp(new WebAppInfo("https://main--zippy-cheesecake-7a1392.netlify.app/shop.html")); 

        row.add(shopButton);
        keyboardRows.add(row);

        replyKeyboardMarkup.setKeyboard(keyboardRows);
        message.setReplyMarkup(replyKeyboardMarkup);

        executeFunction(message);
    }

    private void addUserDB(long chatId, String userName, String firstName, String lastName, String phoneNumber) {
        if (userRepository.findById(chatId).isEmpty()) {
            User user = new User();
            user.setChatId(chatId);
            user.setUserName(userName);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setPhoneNumber(phoneNumber);
            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));

            userRepository.save(user);
        }
    }

    private void addUserProductsDB(long chatId, int productId, int quantity) {
        UserProducts existingUserProducts = userProductsRepository.findByChatIdAndProductId(chatId, productId);

        if (existingUserProducts != null) {
            existingUserProducts.setQuantity(existingUserProducts.getQuantity() + quantity);
            userProductsRepository.save(existingUserProducts);
        } else {
            UserProducts userProducts = new UserProducts();

            if (userProductsRepository.findById(1).isEmpty()) {
                userProducts.setId(1);
            } else {
                var userProducts_all = userProductsRepository.findAll();
                int i = 0;

                for (UserProducts userProducts_i : userProducts_all) {
                    i++;
                }

                userProducts.setId(i + 1);
            }

            userProducts.setChatId(chatId);
            userProducts.setProductId(productId);
            userProducts.setQuantity(quantity);

            userProductsRepository.save(userProducts);
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
