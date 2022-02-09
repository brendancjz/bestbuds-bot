package Command;

import PSQL.PSQL;
import TelegramBot.BirthdayBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.net.URISyntaxException;
import java.sql.SQLException;

public class StartCommand extends Command {
    public StartCommand(BirthdayBot bot, Update update) throws URISyntaxException, SQLException {
        super(bot, update);

    }

    @Override
    public void runCommand() throws TelegramApiException {
        System.out.println("StartCommand.runCommand()");

        SendMessage message = new SendMessage();

        message.setChatId(super.getChatId().toString());
        message.enableHtml(true);

        message.setText("StartCommand.runCommand()");

        super.getBot().execute(message);
    }
}
