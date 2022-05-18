package Command;

import PSQL.PSQL;
import TelegramBot.BestBudsBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.net.URISyntaxException;
import java.sql.SQLException;

public class UpdateDOBCommand extends Command {
    public UpdateDOBCommand(BestBudsBot bot, Update update, PSQL psql) throws URISyntaxException, SQLException {
        super(bot, update, psql);

    }

    @Override
    public void runCommand() {
        try {
            System.out.println("UpdateDOBCommand.runCommand()");
            String text = super.getUpdate().getMessage().getText();
            int chatId = Integer.parseInt(super.getUpdate().getMessage().getChatId().toString());

            SendMessage message = new SendMessage();
            message.setChatId(super.getChatId().toString());
            message.enableHtml(true);

            if (text.equals("/update_dob")) { //Bad command
                missingArgumentsMessage(message);
                return;
            }

            String date = text.substring(12).trim();

            if (validateDate(date) && super.getPSQL().isUserRegistered(chatId)) {
                super.getPSQL().updateUserDOB(chatId, date);
                message.setText("Thanks! Your changed D.O.B is " + date + ".");
                super.getBot().execute(message);

            } else if (!validateDate(date) && super.getPSQL().isUserRegistered(chatId)) {
                wrongDateFormatMessage(message);

            } else {
                notRegisteredMessage(message);
            }
        } catch (SQLException | TelegramApiException throwables) {
            throwables.printStackTrace();
        }


    }
}

