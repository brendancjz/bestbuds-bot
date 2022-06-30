package Command.MessageCommand;

import Command.Command;
import PSQL.PSQL;
import TelegramBot.BestBudsBot;
import com.google.api.client.util.DateTime;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.net.URISyntaxException;
import java.sql.SQLException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ViewMessagesCommand extends Command {
    public static final String COMMAND = "viewMessages";

    public ViewMessagesCommand(BestBudsBot bot, Update update, PSQL psql) throws URISyntaxException, SQLException {
        super(bot, update, psql);
    }

    @Override
    public void runCommand() {
        try {
            System.out.println("ViewMessagesCommand.runCommand()");
            String text = super.getUpdate().getMessage().getText().trim();

            SendMessage message = new SendMessage();
            message.setChatId(super.getChatId().toString());
            message.enableHtml(true);

            if (text.equals("/view_messages")) { //Bad command
                missingArgumentsMessage(message);
                return;
            }

            if (validateText(text)) {
                message.setText("Its good.");
            } else {
                message.setText("Not good");
            }

            super.getBot().execute(message);
        } catch (TelegramApiException throwables) {
            throwables.printStackTrace();
        }
    }

    private boolean validateText(String text) {
        String[] arr = text.split(" ");
        if (arr.length >= 2) {
            String year = arr[1];
            try {
                LocalDate date = LocalDate.of(Integer.parseInt(year), 1, 0);
                return true;
            } catch (DateTimeException e) {
                return false;
            }
        } else {
            return false;
        }
    }
}
