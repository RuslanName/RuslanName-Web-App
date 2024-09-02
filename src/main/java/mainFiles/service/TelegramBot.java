package mainFiles.service;

import mainFiles.config.BotConfig;
import mainFiles.model.differentStates.DifferentStates;
import mainFiles.model.differentStates.DifferentStatesRepository;
import mainFiles.model.user.User;
import mainFiles.model.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.sql.Timestamp;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DifferentStatesRepository differentStatesRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    final BotConfig config;

    static final String HELP_TEXT = boldAndUnderline("СПИСОК КОМАНД") + "\n\n" +
            "/start - запустить бота \n" +
            "/help - показать информацию о возможностях ботa \n";

    static final String OPEN_WEB_APP = "Открыть приложение";

    public TelegramBot(BotConfig config) {
        this.config = config;
        List<BotCommand> listofCommands = new ArrayList<>();
        listofCommands.add(new BotCommand("/start", "запуск бота"));
        listofCommands.add(new BotCommand("/help", "информация о возможностях бота"));
        try {
            this.execute(new SetMyCommands(listofCommands, new BotCommandScopeDefault(), null));
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
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();

            long chatId = update.getMessage().getChatId();

            switch (messageText) {
                case "/start":
                    start(chatId, update.getMessage().getChat().getFirstName());
                    addUserDB(update.getMessage());
                    break;

                case "/help":
                    sendMessage(chatId, HELP_TEXT);
                    break;

                default:
                    sendMessage(chatId, "Извините, данная команда не поддерживается");
            }
        }

        else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();

            long messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();

//            if (callbackData.equals(OPEN_WEB_APP)) {
//
//            }
        }
    }

    private void start(long chatId, String name) {
        SendMessage message = new SendMessage();

        message.setChatId(String.valueOf(chatId));
        message.setText("Привет, " + name);

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        InlineKeyboardButton openAppButton = new InlineKeyboardButton();

        openAppButton.setText(OPEN_WEB_APP);
        openAppButton.setUrl("http://localhost:63342/Telegram%20app/Base/mainFiles/web/index.html?_ijt=5fia9tkvdb84u6oak24p53aag8&_ij_reload=RELOAD_ON_SAVE");

        rowInline.add(openAppButton);
        rowsInline.add(rowInline);

        inlineKeyboardMarkup.setKeyboard(rowsInline);
        message.setReplyMarkup(inlineKeyboardMarkup);

        executeFunction(message);
    }

    private void addUserDB(Message message) {
        if (userRepository.findById(message.getChatId()).isEmpty()) {
            var chat = message.getChat();

            User user = new User();

            user.setChatId(message.getChatId());
            user.setUserName(chat.getUserName());
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));

            userRepository.save(user);

            log.info("user saved: " + user);
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

    private void executeMessage(long chatId, long messageId, Object text) {
        EditMessageText message = new EditMessageText();

        message.enableHtml(true);
        message.setChatId(String.valueOf(chatId));
        message.setText(text.toString());
        message.setMessageId((int) messageId);

        executeFunction(message);
    }

    private void executeFunction(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error: " + e.getMessage());
        }
    }

    private void executeFunction(EditMessageText message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error: " + e.getMessage());
        }
    }
}
