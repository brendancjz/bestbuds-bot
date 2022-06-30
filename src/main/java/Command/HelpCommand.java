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

        help += "<b>Help</b>\n\n";
        help += "<u>Where can I find the set up steps?</u>\n";
        help += "Enter /start command to get started.\n\n";
        help += "<u>What do I do after following the set up steps?</u>\n";
        help += "You're all set! Await a message from BestBuds Bot.\n\n";
        help += "<u>How can I see my friends code?</u>\n";
        help += "Use /view_bestbuds command and select the common BestBuds group!\n\n";
        help += "<u>Where can I clarify my doubts or give feedback?</u>\n";
        help += "Reach out to @brendanchia!\n\n\n";
        help += "<em>BestBuds Bot is in BETA. Do give feedback or suggestions to improve the bot. :)</em>";

        return help;
    }
}
