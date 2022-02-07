import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.print.attribute.URISyntax;
import javax.swing.text.DateFormatter;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;

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
        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                SendMessage message = new SendMessage();

                message.setChatId(update.getMessage().getChatId().toString());
                message.enableHtml(true);

                int chatId = Integer.parseInt(update.getMessage().getChatId().toString());
                PSQL psql = new PSQL();
                //Personal Chats have positive chatId while Group Chats have negative chatId
                if (chatId > 0) {
                    personalChatMessage(message, update, chatId, psql);
                } else {
                    groupChatMessage(message);
                }

                psql.closeConnection();
            }
        } catch (SQLException | URISyntaxException throwables) {
            throwables.printStackTrace();
        }

    }

    private void groupChatMessage(SendMessage message) {
        try {
            message.setText("Sorry. This bot currently does not support group messaging.");

            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void personalChatMessage(SendMessage message, Update update, int chatId, PSQL psql) {
        try {
            String name = update.getMessage().getChat().getFirstName();
            String text = update.getMessage().getText();


            //Universal Commands. No need to update Query and check User.
            if (text.startsWith("/start")) {
                System.out.println("=== Start Event Called === ");

                String startMsg = generateIntro(name);
                if (psql.isUserRegistered(chatId)) {
                    startMsg += " <em>It looks like you are already registered in the database!</em>";
                }

                message.setText(startMsg);

            } else if (text.startsWith("/addDOB")) {
                String date = text.substring(8);

                if (validateDate(date) && !psql.isUserRegistered(chatId)) {
                    psql.addNewUser(chatId, text);
                    message.setText("Thanks! Your D.O.B is " + date);
                } else if (validateDate(date) && psql.isUserRegistered(chatId)) {
                    psql.updateUserDOB(chatId, text);
                    message.setText("Thanks! Your changed D.O.B is " + date);
                }

            } else if (text.startsWith("/getDOB")) {
                String date = psql.getUserDOB(chatId);
                message.setText("Your D.O.B is " + date);
            } else {
                message.setText("Echo: " + name + " said " + text);
                
            }


            execute(message);
        } catch (SQLException | TelegramApiException | ParseException throwables) {
            throwables.printStackTrace();
        }


    }

    public boolean validateDate(String date) {
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        try {
            Date d = dateFormat.parse(date);
        } catch (ParseException e) {
            return false;
        }
        return true;

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
