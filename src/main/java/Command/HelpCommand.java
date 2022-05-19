package Command;

import PSQL.PSQL;
import TelegramBot.BestBudsBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.net.URISyntaxException;
import java.sql.SQLException;

public class HelpCommand extends Command {
    public HelpCommand(BestBudsBot bot, Update update, PSQL psql) throws URISyntaxException, SQLException {
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

        help += "<b>Help</b>\n";
        help += this.generateBBB();
        help += "For BestBuds Group use cases, check out /subscribe for more information.\n";
        help += "For User use cases, check out /profile for more information.\n\n";
        help += "<b>FAQ</b>\n\n";
        help += "<u>What can this bot do?</u>\n";
        help += this.generateBotDescription();
        help += "<u>No BestBuds Group?</u>\n";
        help += "No worries, create a BestBuds Group. You will be the owner of the group. Enter /subscribe for more information.";

//        help += "<em>Commands: </em>\n";
//        help += "/subscribe {FIRST_NAME} {D.O.B (dd-MM-yyyy)}\n";
//        help += "/update_dob {D.O.B (dd-MM-yyyy)}\n";
//        help += "/update_name {FIRST_NAME}\n\n";
//        help += "<em>Example:</em>\n";
//        help += "/subscribe Brendan 01-01-1999\n";
//        help += "/update_dob 12-05-1999\n";
//        help += "/update_name Bren\n\n";

        return help;
    }
}
