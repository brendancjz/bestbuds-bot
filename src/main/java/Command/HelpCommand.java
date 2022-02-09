package Command;

import PSQL.PSQL;
import TelegramBot.BirthdayBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.net.URISyntaxException;
import java.sql.SQLException;

public class HelpCommand extends Command {
    public HelpCommand(BirthdayBot bot, Update update, PSQL psql) throws URISyntaxException, SQLException {
        super(bot, update, psql);

    }

    @Override
    public void runCommand() {
        try {
            System.out.println("HelpCommand.runCommand()");

            SendMessage message = new SendMessage();

            message.setChatId(super.getChatId().toString());
            message.enableHtml(true);

            message.setText(generateHelp());
            super.getBot().execute(message);

        } catch (TelegramApiException throwables) {
            throwables.printStackTrace();
        }


    }

    private String generateHelp() {
        String help = "";

        help += "<b>Help</b> \n\n";
        help += "<em>Commands: </em>\n";
        help += "/subscribe {FIRST_NAME} {D.O.B (dd-MM-yyyy)}\n";
        help += "/update_dob {D.O.B (dd-MM-yyyy)}\n";
        help += "/update_name {FIRST_NAME}\n\n";
        help += "<em>Example:</em>\n";
        help += "/subscribe Brendan 01-01-1999\n";
        help += "/update_dob 12-05-1999\n";
        help += "/update_name Bren\n\n";

        return help;
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
