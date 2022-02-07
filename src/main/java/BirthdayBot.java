import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.net.URISyntaxException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BirthdayBot extends TelegramLongPollingBot {

    @Override
    public String getBotUsername() {
        return System.getenv("BOT_USERNAME");
    }

    @Override
    public String getBotToken() {
        return System.getenv("BOT_TOKEN");
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
                executeMessage(message);
                return;
            }

            if (text.startsWith("/help")) {
                System.out.println("=== Help Event Called === ");

                message.setText(generateIntro("Brendan"));
                executeMessage(message);
                return;
            }

            if (text.startsWith("/subscribe")) {
                if (text.equals("/subscribe")) { //Bad command
                    missingArgumentsMessage(message);
                    return;
                }

                String[] arr = text.split(" ");

                if (arr.length == 3) {
                    String firstName = arr[1];
                    String date = arr[2];

                    if (validateDate(date) && !psql.isUserRegistered(chatId)) {
                        psql.addNewUser(chatId, firstName, date);
                        message.setText("Thanks! Your name is " + firstName + " and your D.O.B is " + date + ".");
                        executeMessage(message);

                        scheduleBirthdayMessage(chatId);
                    } else if (psql.isUserRegistered(chatId)) {
                        message.setText("You have already been registered.");

                        executeMessage(message);
                    } else {
                        wrongDateFormatMessage(message);
                    }


                } else {
                    missingArgumentsMessage(message);
                }

            } else if (text.startsWith("/update_dob")) {
                if (text.equals("/update_dob")) { //Bad command
                    missingArgumentsMessage(message);
                    return;
                }

                String date = text.substring(10);

                if (validateDate(date) && psql.isUserRegistered(chatId)) {
                    psql.updateUserDOB(chatId, date);
                    message.setText("Thanks! Your changed D.O.B is " + date + ".");

                    executeMessage(message);
                } else if (!validateDate(date) && psql.isUserRegistered(chatId)) {
                    wrongDateFormatMessage(message);
                } else {
                    notRegisteredMessage(message);
                }

            } else if (text.startsWith("/update_name")) {
                if (text.equals("/update_name")) { //Bad command
                    missingArgumentsMessage(message);
                    return;
                }

                String firstName = text.substring(11).trim();

                if (psql.isUserRegistered(chatId)) {
                    psql.updateUserName(chatId, firstName);
                    message.setText("Thanks! Your changed name is " + firstName + ".");

                    executeMessage(message);
                } else {
                    notRegisteredMessage(message);
                }

            } else if (text.startsWith("/getDOB")) {
                String date = psql.getUserDOB(chatId);
                message.setText("Your D.O.B is " + date);

                executeMessage(message);
            } else if (text.startsWith("/getName")) {
                String firstName = psql.getUserName(chatId);
                message.setText("Your name is " + firstName);

                executeMessage(message);
            } else {
                message.setText("Invalid Command.");

                executeMessage(message);
            }


        } catch (SQLException | ParseException throwables) {
            throwables.printStackTrace();
        }


    }

    private void notRegisteredMessage(SendMessage message) {
        message.setText("You're not registered yet.");
        executeMessage(message);
    }

    private void wrongDateFormatMessage(SendMessage message) {
        message.setText("Wrong date format. Try again with dd-MM-yyyy");
        executeMessage(message);
    }

    private void missingArgumentsMessage(SendMessage message) {
        message.setText("Bad command. Missing arguments.");
        executeMessage(message);
    }

    private void executeMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void scheduleBirthdayMessage(int chatId) {
        //TODO: The scheduling
    }

    public boolean validateDate(String date) {
        DateFormat dateFormat1 = new SimpleDateFormat("dd-MM-yyyy");

        try {
            Date d = dateFormat1.parse(date);
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

    public static String generateHelp() {
        String help = "";

        help += "<b>Help</b> \n\n";
        help += "<em>Commands:</em>\n";
        help += "/subscribe <FIRST_NAME> <D.O.B {dd/MM/yyyy}>\n";
        help += "/update_dob <D.O.B {dd-MM-yyyy}>\n";
        help += "/update_name <FIRST_NAME>\n\n";
        help += "<em>Example:</em>\n";
        help += "/subscribe Brendan 01-01-1999\n";
        help += "/update_dob 12-05-1999\n";
        help += "/update_name Bren\n\n";

        return help;
    }
}
