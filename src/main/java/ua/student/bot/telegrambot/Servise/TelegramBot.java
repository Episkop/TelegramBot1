package ua.student.bot.telegrambot.Servise;

import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
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
import ua.student.bot.telegrambot.Config.BotConfig;
import ua.student.bot.telegrambot.Config.Model.User;
import ua.student.bot.telegrambot.Config.Model.UserRepository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {
    private UserRepository userRepository;
    private final BotConfig botConfig;
    static final String HELP_TEXT = "This bot was created to demonstration Spring capabilities.\n\n"+
            "You can execute commands from the main menu on the left or by typing a command:\n\n" +
            "Type /start to see a welcome message\n\n" +
            "Type /mydata to see data stored about yourself\n\n" +
            "Type /help to see this message again";
    public TelegramBot(UserRepository userRepository, BotConfig botConfig) {
        this.userRepository = userRepository;
        this.botConfig = botConfig;
//        Создание меню
        List<BotCommand> commandList = new ArrayList<>();
        commandList.add(new BotCommand("/start", "welcoming comment"));
        commandList.add(new BotCommand("/mydata", "get your data info"));
        commandList.add(new BotCommand("/deletedata", "delete my data"));
        commandList.add(new BotCommand("/help", "info how to use bot"));
        commandList.add(new BotCommand("/setting" , "set your preferences"));
        commandList.add(new BotCommand("/register", "Yours registration in the bot"));
        try{
            execute(new SetMyCommands(commandList,new BotCommandScopeDefault(),null));
        }catch (TelegramApiException e){
            log.error("Error setting bot`s command list: " +e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getDotToken();
    }
 //Что должен делать бот, когда пользователь посылает запрос на него. Update содержит текст сообщения от пользователя.
//    А также инфу о пользоватиле. И много другого
    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
           long chatId = update.getMessage().getChatId();


            switch (messageText){
                case "/start":

                    addUser(update.getMessage());
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                    break;
                case "/help":
                    sendMessage(chatId,HELP_TEXT);
                    break;
                case "/register":
                    register(chatId);
                    break;
                default:
                    sendMessage(chatId,"Sorry, the command was not recognized");

            }
//      Проверка на то что пришол не текст ка срабатывание кнопок (непосредственно под сообщением)
        } else if (update.hasCallbackQuery()) {
            String callBackData = update.getCallbackQuery().getData();
            long messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();

            if(callBackData.equals("YES_BUTTON")){
                String text = "You press YES";
                EditMessageText message = new EditMessageText();//Меняем текст сообщения. Стого где есть вопрос и кнопки. На новый.
                message.setChatId(chatId);
                message.setText(text);
                message.setMessageId((int) messageId);

                try {
                    execute(message);
                }catch (TelegramApiException e){
                    log.error("Error occurred: " + e.getMessage());
                }
            }

            if(callBackData.equals("NO_BUTTON")){
                String text = "You press NO";
                EditMessageText message = new EditMessageText();//Меняем текст сообщения. Стого где есть вопрос и кнопки. На новый.
                message.setChatId(chatId);
                message.setText(text);
                message.setMessageId((int) messageId);

                try {
                    execute(message);
                }catch (TelegramApiException e){
                    log.error("Error occurred: " + e.getMessage());
                }
            }
        }
    }
//Создание кнопок непосредственно под всплывающем сообщением (ответом) от бота с вариантами выбора
    private void register(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Do you want registration?");

        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();// Спилок списков рядов кнопок
        List<InlineKeyboardButton> rowInLine = new ArrayList<>(); //Список кнопок для 1 ряда
        var yesButton = new InlineKeyboardButton();//Сами кнопки ряда

        yesButton.setText("YES");// Это кнопка
        yesButton.setCallbackData("YES_BUTTON");// Это идентификатор Кнопни. Его уникальный ID. Для понимания ботом какая кнопка нажата.

        var noButton = new InlineKeyboardButton();//Сами кнопки ряда

        noButton.setText("NO");// Это кнопка
        noButton.setCallbackData("NO_BUTTON");// Это идентификатор Кнопни. Его уникальный ID. Для понимания ботом какая кнопка нажата.

        rowInLine.add(yesButton);// Добовляем кнопки в список
        rowInLine.add(noButton);

        rowsInLine.add(rowInLine);// список в список списков
        markupInLine.setKeyboard(rowsInLine);
        message.setReplyMarkup(markupInLine);

        try {
            execute(message);
        }catch (TelegramApiException e){
            log.error("Error occurred: " + e.getMessage());
        }
    }

    private void addUser(Message msg) {

        if(userRepository.findById(msg.getChatId()).isEmpty()){
            var chatId = msg.getChatId();
            var chat = msg.getChat();
            User user = new User();

            user.setChatId(chatId);
            user.setUserName(chat.getUserName());
            user.setLastName(chat.getLastName());
            user.setRegistration(new Timestamp(System.currentTimeMillis()));

            userRepository.save(user);
        }
    }


    //    Ответ на нажатие /start
    private void startCommandReceived (long chatId,String name){
// добавил смайлик!!! blush
        String answer = EmojiParser.parseToUnicode("Hi ," + name + ", nice to meet you!" + ":blush:");
//        String answer = "Hi ," + name + ", nice to meet you!";
        log.info("Replied to user " + name);
        sendMessage(chatId,answer);
    }
    private void sendMessage (long chatId, String textToSend){

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(textToSend);
// кнопки меню вертуальной клавиатуры
        ReplyKeyboardMarkup keyboardMarkup =new ReplyKeyboardMarkup();
        List<KeyboardRow> rowList = new ArrayList<>();
        KeyboardRow keyboardButtons = new KeyboardRow();

        keyboardButtons.add("weather");
        keyboardButtons.add("joke");

        rowList.add(keyboardButtons);

        keyboardButtons = new KeyboardRow();

        keyboardButtons.add("register");
        keyboardButtons.add("check my data");
        keyboardButtons.add("delete my data");

        rowList.add(keyboardButtons);
        keyboardMarkup.setKeyboard(rowList);

        message.setReplyMarkup(keyboardMarkup);
        try {
            execute(message);
        }catch (TelegramApiException e){
            log.error("Error occurred: " + e.getMessage());
        }


    }
}
