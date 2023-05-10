package ua.student.bot.telegrambot.Servise;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ua.student.bot.telegrambot.Config.BotConfig;

@Component
public class TelegramBot extends TelegramLongPollingBot {

    private final BotConfig botConfig;

    public TelegramBot(BotConfig botConfig) {
        this.botConfig = botConfig;
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
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                    break;
                default:
                    sendMessage(chatId,"Sorry, the command was not recognized");

            }
        }
    }
//    Ответ на нажатие /start
    private void startCommandReceived (long chatId,String name){

        String answer = "Hi ," + name + ", nice to meet you!";

        sendMessage(chatId,answer);
    }
    private void sendMessage (long chatId, String textToSend){

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(textToSend);

        try {
            execute(message);
        }catch (TelegramApiException e){

        }


    }
}
