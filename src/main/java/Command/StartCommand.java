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
    public void runCommand() {
        try {
            System.out.println("StartCommand.runCommand()");

            SendMessage message = new SendMessage();

            message.setChatId(super.getChatId().toString());
            message.enableHtml(true);

            message.setText("StartCommand.runCommand()");

            String startMsg = generateIntro(super.getFistName());

            if (super.getPSQL().isUserRegistered(super.getChatId())) {
                startMsg += " <em>It looks like you are already registered in the database!</em>";
            }

            message.setText(startMsg);
            super.getBot().execute(message);

        } catch (SQLException | TelegramApiException throwables) {
            throwables.printStackTrace();
        }


    }

    private String generateIntro(String name) {
        String intro = "<b>Start</b> \n\n";

        intro += "Hi " + name +
                "! Welcome to StickyFaith Birthday Bot.\n\n";
        intro += "This bot stores everyone's birthday and encourages you to send a birthday wish to some whoever's birthday is around the corner.\n\n";
        intro += "Type /help to see what this bot can do.\n\n" +
                "Curious how the bot stores your data? Well, it uses PostgreSQL! It is an open-source database system with solid capabilities. PSQL.PSQL is reliable and " +
                "your data is stored in PSQL.PSQL's encrypted databases, so nothing to worry about!\n\n";
        intro += "<em>You have established a connection with the server. This connection is 24/7.</em>";

        return intro;
    }
}
