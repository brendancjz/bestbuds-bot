import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main {
    public static void main(String[] args ) {
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            BirthdayBot birthdayBot = new BirthdayBot();
            telegramBotsApi.registerBot(birthdayBot); //botSession has started.

        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
