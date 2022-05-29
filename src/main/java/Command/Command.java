package Command;

import PSQL.PSQL;
import TelegramBot.BestBudsBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.net.URISyntaxException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Command {
    private final BestBudsBot bot;
    private final PSQL psql;
    private final Update update;

    public Command(BestBudsBot bot, Update update, PSQL psql) throws URISyntaxException, SQLException {
        this.bot = bot;
        this.update = update;
        this.psql = psql;
    }

    public Integer getChatId() {
        if (this.update.getMessage() != null) {
            return Integer.parseInt(this.update.getMessage().getChatId().toString());
        } else {
            return Integer.parseInt(this.update.getCallbackQuery().getMessage().getChatId().toString());
        }
    }

    public String getFirstName() {
        return this.update.getMessage().getChat().getFirstName();
    }

    public String getUsername() {
        return this.update.getMessage().getChat().getUserName();
    }

    public Update getUpdate() {
        return this.update;
    }

    public BestBudsBot getBot() {
        return this.bot;
    }

    public PSQL getPSQL() {
        return this.psql;
    }

    public void runCommand() {
        System.out.println("Command runCommand()");
    }

    public void notRegisteredMessage(SendMessage message) throws TelegramApiException {
        message.setText("You're not registered yet.");
        this.bot.execute(message);
    }

    public void wrongDateFormatMessage(SendMessage message) throws TelegramApiException {
        message.setText("Wrong date format. Try again with dd-MM-yyyy");
        this.bot.execute(message);
    }

    public void missingArgumentsMessage(SendMessage message) throws TelegramApiException {
        message.setText("Bad command. Missing arguments.");
        this.bot.execute(message);
    }

    public void invalidMessage(SendMessage message, String text) throws TelegramApiException {
        message.setText("Bad Command: " + text + ". Enter /help for assistance.");
        this.bot.execute(message);
    }

    public boolean validateDate(String date) {
        DateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd");
//        DateFormat dateFormat2 = new SimpleDateFormat("dd-MM-yyyy");
//        DateFormat dateFormat3 = new SimpleDateFormat("yyyy/MM/dd");
//        DateFormat dateFormat4 = new SimpleDateFormat("dd/MM/yyyy");

        try {
            Date d = dateFormat1.parse(date);
//            Date d2 = dateFormat2.parse(date);
//            Date d3 = dateFormat3.parse(date);
//            Date d4 = dateFormat4.parse(date);
        } catch (ParseException e) {
            return false;
        }
        return true;

    }

    public String generateBBB() {
        return  "<pre> _     _     _\n" +
                "| |   | |   | |\n" +
                "| |__ | |__ | |__\n" +
                "|  _ \\|  _ \\|  _ \\\n" +
                "| (_) | (_) | (_) |\n" +
                "|____/|____/|____/</pre>\n\n";
    }

    public String generateBotDescription() {
        String desc = "";
        desc += "BestBuds Bot helps to remember all your friends birthdays! \uD83C\uDF89 \uD83C\uDF82 " +
                "It will remind you to send a birthday wish to them nearing their birthday and it will " +
                "collate all your birthday wishes too!\n\n";

        return desc;
    }

    public void runCallback() {
        System.out.println("Command runCallback()");
    }
}
