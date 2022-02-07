import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.print.attribute.URISyntax;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.ParseException;

public class BirthdayBot extends TelegramLongPollingBot {

    @Override
    public String getBotUsername() {
        return "sf_bday_bot";
    }

    @Override
    public String getBotToken() {
        return "5131673643:AAGhxUIkrAhT8yzrJj7EnWZZOwiEsroRqA0";
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            SendMessage message = new SendMessage();

            message.setChatId(update.getMessage().getChatId().toString());
            message.enableHtml(true);

            int chatId = Integer.parseInt(update.getMessage().getChatId().toString());

            //Personal Chats have positive chatId while Group Chats have negative chatId
            if (chatId > 0) {
                personalChatMessage(message, update, chatId);

            } else {
                groupChatMessage(message, update);
            }
        }
    }

    private void groupChatMessage(SendMessage message, Update update) {
        return;
    }

    private void personalChatMessage(SendMessage message, Update update, int chatId) {
        try {
            String name = update.getMessage().getChat().getFirstName();
            String text = update.getMessage().getText();
            PSQL psql = new PSQL();

            //Universal Commands. No need to update Query and check User.
            if (text.startsWith("/start")) {
                System.out.println("=== Start Event Called === ");

                String startMsg = generateIntro(name);


                if (psql.isUserRegistered(chatId)) {
                    startMsg += " <em>It looks like you are already registered in the database!</em>";
                }

                psql.closeConnection();

                message.setText(startMsg);

            } else {

                if (text.startsWith("/addDOB")) {
                    text = text.substring(8);

                    psql.addNewUser(chatId, text);
                    message.setText("Successfully added DOB.");
                } else {
                    message.setText("Echo: " + name + " said " + text);
                }
            }

            execute(message);
        } catch (SQLException | URISyntaxException | TelegramApiException | ParseException throwables) {
            System.out.println("Something happened");
            throwables.printStackTrace();
        }


    }

    public static String generateIntro(String name) {
        String intro = "<b>Start</b> \n\n";

        intro += "Hi " + name +
                "! Welcome to StickyFaith Birthday Bot.\n\n";
        intro += "This bot stores everyone's birthday and encourages you to send a birthday wish to some whoever's birthday is around the corner.\n\n";
        intro += "Type /help to see what this bot can do.\n\n" +
                "Curious how the bot stores your data? Well, it uses PostgreSQL! It is an open-source database system with solid capabilities. PSQL is reliable and " +
                "your data is stored in PSQL's encrypted databases, so nothing to worry about!\n\n";
        intro += "<em>You have established a connection with the server. This connection is 24/7.</em>";

        return intro;
    }
}
