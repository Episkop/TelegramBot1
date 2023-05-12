package ua.student.bot.telegrambot.Servise;

import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
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
        List<BotCommand> commandList = new ArrayList<>();
        commandList.add(new BotCommand("/start", "welcoming comment"));
        commandList.add(new BotCommand("/mydata", "get your data info"));
        commandList.add(new BotCommand("/deletedata", "delete my data"));
        commandList.add(new BotCommand("/help", "info how to use bot"));
        commandList.add(new BotCommand("/setting" , "set your preferences"));
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
                default:
                    sendMessage(chatId,"Sorry, the command was not recognized");

            }
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

        try {
            execute(message);
        }catch (TelegramApiException e){
            log.error("Error occurred: " + e.getMessage());
        }


    }
}
