package Command;

import PSQL.PSQL;
import TelegramBot.BirthdayBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.net.URISyntaxException;
import java.sql.SQLException;

public class UpdateNameCommand extends Command {
    public UpdateNameCommand(BirthdayBot bot, Update update, PSQL psql) throws URISyntaxException, SQLException {
        super(bot, update, psql);

    }

    @Override
    public void runCommand() {
        try {
            System.out.println("UpdateNameCommand.runCommand()");
            String text = super.getUpdate().getMessage().getText();
            int chatId = Integer.parseInt(super.getUpdate().getMessage().getChatId().toString());

            SendMessage message = new SendMessage();
            message.setChatId(super.getChatId().toString());
            message.enableHtml(true);

            if (text.equals("/update_name")) { //Bad command
                missingArgumentsMessage(message);
                return;
            }

            String firstName = text.substring(13).trim();

            if (super.getPSQL().isUserRegistered(chatId)) {
                super.getPSQL().updateUserName(chatId, firstName);
                message.setText("Thanks! Your changed name is " + firstName + ".");
                super.getBot().execute(message);

            } else {
                notRegisteredMessage(message);
            }
        } catch (SQLException | TelegramApiException throwables) {
            throwables.printStackTrace();
        }


    }
}

