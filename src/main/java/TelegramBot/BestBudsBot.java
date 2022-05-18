package TelegramBot;

import Command.*;
import PSQL.*;
import Timer.*;
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

public class BestBudsBot extends TelegramLongPollingBot {

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
            String text = update.getMessage().getText();

            Command command;
            //Universal Commands. No need to update Query and check User.
            if (text.startsWith("/start")) {
                System.out.println("=== Start Event Called === ");
                command = new StartCommand(this, update, psql);
                command.runCommand();
                return;
            }

            if (text.startsWith("/help")) {
                System.out.println("=== Help Event Called === ");
                command = new HelpCommand(this, update, psql);
                command.runCommand();
                return;
            }

            if (text.startsWith("/subscribe")) {
                System.out.println("=== Subscribe Event Called === ");
                command = new SubscribeCommand(this, update, psql);
                command.runCommand();
            }
            else if (text.startsWith("/update_dob")) {
                System.out.println("=== Update DOB Event Called === ");
                command = new UpdateDOBCommand(this, update, psql);
                command.runCommand();
            }
            else if (text.startsWith("/update_name")) {
                System.out.println("=== Update Name Event Called === ");
                command = new UpdateNameCommand(this, update, psql);
                command.runCommand();
            }
            else if (text.startsWith("/getDOB")) {
                String date = psql.getUserDOB(chatId);
                message.setText("Your D.O.B is " + date);
                executeMessage(message);
            }
            else if (text.startsWith("/getName")) {
                String firstName = psql.getUserName(chatId);
                message.setText("Your name is " + firstName);
                executeMessage(message);
            } else {
                message.setText("Invalid Command: " + text);
                executeMessage(message);
            }
        } catch (SQLException | URISyntaxException throwables) {
            throwables.printStackTrace();
        }
    }

    private void executeMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void runScheduleHappyBirthdayMessage(int chatId) {
        try {
            PSQL psql = new PSQL();

            SendMessage message = new SendMessage();
            message.setChatId("" + chatId);

            message.setText("Happy Birthday!");

            execute(message);
            psql.closeConnection();
        } catch (URISyntaxException | SQLException | TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
