package Command;

import PSQL.PSQL;
import TelegramBot.BirthdayBot;
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
    private final BirthdayBot bot;
    private final PSQL psql;
    private final Update update;

    public Command(BirthdayBot bot, Update update, PSQL psql) throws URISyntaxException, SQLException {
        this.bot = bot;
        this.update = update;
        this.psql = psql;
    }

    public Integer getChatId() {
        return Integer.parseInt(this.update.getMessage().getChatId().toString());
    }

    public String getFistName() {
        return this.update.getMessage().getChat().getFirstName();
    }

    public String getUsername() {
        return this.update.getMessage().getChat().getUserName();
    }

    public Update getUpdate() {
        return this.update;
    }

    public BirthdayBot getBot() {
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

    public boolean validateDate(String date) {
        DateFormat dateFormat1 = new SimpleDateFormat("dd-MM-yyyy");

        try {
            Date d = dateFormat1.parse(date);
        } catch (ParseException e) {
            return false;
        }
        return true;

    }
}
