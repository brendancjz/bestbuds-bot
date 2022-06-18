package Command;

import PSQL.PSQL;
import TelegramBot.BestBudsBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import resource.Entity.User;

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

    public void runCallback() {
        System.out.println("Command runCallback()");
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

        try {
            Date d = dateFormat1.parse(date);
        } catch (ParseException e) {
            return false;
        }
        return true;
    }

    public Boolean validateGroupCode(String text) throws SQLException {
        String[] arr = text.split(" ");
        return arr.length == 2 && !psql.isGroupCodeUnique(arr[1]);
    }

    public Boolean validateGroupCodeAndUserInGroup(String text, Integer chatId) throws SQLException {
        String[] arr = text.split(" ");

        return arr.length == 2 && !psql.isGroupCodeUnique(arr[1]) && psql.isUserInGroup(chatId, arr[1]);
    }

    public Boolean validateUserCodeGroupCodeAndUserInGroup(String text) throws SQLException {
        String[] arr = text.split(" ");

        return arr.length == 3 &&
                !User.isNull(psql.getUserDataResultSet(arr[1])) &&
                psql.isUserInGroup(psql.getUserDataResultSet(arr[1]).chatId, arr[2]);
    }

    public boolean validateMessage(String text) throws SQLException {
        String[] arr = text.split(" ");
        String receiverCode = arr[1];

        return arr.length >= 3 && !User.isNull(psql.getUserDataResultSet(receiverCode));
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


}
