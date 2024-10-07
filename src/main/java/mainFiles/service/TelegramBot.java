package mainFiles.service;

import mainFiles.config.BotConfig;
import mainFiles.model.registrationState.RegistrationState;
import mainFiles.model.registrationState.RegistrationStateRepository;
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
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;



@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RegistrationStateRepository registrationStateRepository;

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
            String text = message.getText();

            if (text.equals("/start")) {
                start(chatId);
            }

            else if (registrationStateRepository.existsById(chatId) && registrationStateRepository.findById(chatId).get().getUserName() == null) {
                registration(chatId, "name", text);
            }

            else if (registrationStateRepository.existsById(chatId) && registrationStateRepository.findById(chatId).get().getPhoneNumber() == null) {
                registration(chatId, "phone", text);
            }
        }
    }

    private void start(long chatId) {
        if (userRepository.findById(chatId).isPresent()) {
            User user = userRepository.findById(chatId).get();
            sendMessage(chatId, "Здравствуйте, " + user.getUserName() + ".");
            sendShopButton(chatId);
        }

        else {
            if (!registrationStateRepository.existsById(chatId)) {
                RegistrationState registrationState = new RegistrationState();
                registrationState.setChatId(chatId);
                registrationStateRepository.save(registrationState);
            }

            sendMessage(chatId, "Здравствуйте! Вам нужно зарегистрироваться.");
            sendMessage(chatId, "Шаг 1. Введите ваше имя (никнейм).");
        }
    }

    private void registration(long chatId, String step, String input) {
        RegistrationState registrationState = registrationStateRepository.findById(chatId).get();

        if (step.equals("name")) {
            registrationState.setUserName(input);
            registrationStateRepository.save(registrationState);
            sendMessage(chatId, "Шаг 2. Введите свой номер телефона без пробелов (начало +7 или 8).");
        }

        else if (step.equals("phone")) {
            if (!((input.startsWith("+7") || input.startsWith("8")) && input.length() > 11)) {
                sendMessage(chatId, "Номер введен неправильно. Введите правильный номер.");
                return;
            }

            registrationState.setPhoneNumber(input);
            registrationStateRepository.save(registrationState);

            addUserDB(chatId, registrationState.getUserName(), registrationState.getPhoneNumber());
        }
    }

    private void sendShopButton(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Перейдите в магазин");

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);

        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();

        KeyboardButton shopButton = new KeyboardButton("Магазин");
        shopButton.setWebApp(new WebAppInfo("https://magazin-ruslanname.amvera.io/shop.html"));

        row.add(shopButton);
        keyboardRows.add(row);

        replyKeyboardMarkup.setKeyboard(keyboardRows);
        message.setReplyMarkup(replyKeyboardMarkup);

        executeFunction(message);
    }

    private void addUserDB(long chatId, String userName, String phoneNumber) {
        if (userRepository.findById(chatId).isEmpty()) {
            User user = new User();
            user.setChatId(chatId);
            user.setUserName(userName);
            user.setPhoneNumber(phoneNumber);
            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));

            userRepository.save(user);

            registrationStateRepository.deleteById(chatId);

            sendShopButton(chatId);
        }

        else {
            sendShopButton(chatId);
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
