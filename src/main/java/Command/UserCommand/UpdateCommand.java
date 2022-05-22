package Command.UserCommand;

import Command.Command;
import PSQL.PSQL;
import TelegramBot.BestBudsBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.net.URISyntaxException;
import java.sql.SQLException;

public class UpdateCommand extends Command {

    public UpdateCommand(BestBudsBot bot, Update update, PSQL psql) throws URISyntaxException, SQLException {
        super(bot, update, psql);
    }

    @Override
    public void runCommand() {
        try {
            System.out.println("UpdateCommand.runCommand()");
            String text = super.getUpdate().getMessage().getText();
            int chatId = Integer.parseInt(super.getUpdate().getMessage().getChatId().toString());

            SendMessage message = new SendMessage();
            message.setChatId(super.getChatId().toString());
            message.enableHtml(true);

            if (text.equals("/update") || text.equals("/update_name") ||
                    text.equals("/update_dob") || text.equals("/update_desc")) { //Bad command
                missingArgumentsMessage(message);
                return;
            }

            String actualCommand = text.split(" ")[0];

//            if (super.getPSQL().isUserRegistered(chatId)) {
//                super.getPSQL().updateUserName(chatId, firstName);
//                message.setText("Thanks! Your changed name is " + firstName + ".");
//                super.getBot().execute(message);
//
//            } else {
//                notRegisteredMessage(message);
//            }
            message.setText(actualCommand);
            super.getBot().execute(message);

        } catch (TelegramApiException throwables) {
            throwables.printStackTrace();
        }
    }
}
