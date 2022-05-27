import TelegramBot.BestBudsBot;
import Timer.HappyBirthdayTimer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.net.URISyntaxException;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args ) {
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            BestBudsBot bestBudsBot = new BestBudsBot();
            telegramBotsApi.registerBot(bestBudsBot); //botSession has started.

            //Send Happy Birthday
//            HappyBirthdayTimer timer = new HappyBirthdayTimer(bestBudsBot);
//            timer.start();
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
