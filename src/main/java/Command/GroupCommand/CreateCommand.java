package Command.GroupCommand;

import Command.Command;
import PSQL.PSQL;
import TelegramBot.BestBudsBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.net.URISyntaxException;
import java.sql.SQLException;
import java.text.ParseException;

public class CreateCommand extends Command {

    public CreateCommand(BestBudsBot bot, Update update, PSQL psql) throws URISyntaxException, SQLException {
        super(bot, update, psql);

    }

    @Override
    public void runCommand() {
        try {
            System.out.println("CreateCommand.runCommand()");
            String text = super.getUpdate().getMessage().getText();
            int chatId = Integer.parseInt(super.getUpdate().getMessage().getChatId().toString());

            SendMessage message = new SendMessage();
            message.setChatId(super.getChatId().toString());
            message.enableHtml(false);

            if (text.equals("/create")) { //Bad command. Missing arguments
                missingArgumentsMessage(message);
                return;
            }

            String[] arr = text.split(" ");

            if (arr.length != 2) {
                invalidMessage(message, text);
                return;
            }


            String groupName = arr[1];

            message.setText("Group Name: " + groupName);

            //TODO Use Callback to confirm the group name and creation of group
            //TODO add this new group into the db
            //TODO add the user into this db. If user, not register, register the man in there.
            
            super.getBot().execute(message);

        } catch (TelegramApiException throwables) {
            throwables.printStackTrace();
        }
    }
}
