package Command;

import PSQL.PSQL;
import TelegramBot.BirthdayBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.net.URISyntaxException;
import java.sql.SQLException;

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
}
