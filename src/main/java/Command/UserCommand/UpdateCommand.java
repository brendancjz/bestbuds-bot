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
            runActualCommand(message, text);


        } catch (TelegramApiException | SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private void runActualCommand(SendMessage message, String text) throws TelegramApiException, SQLException {
        String[] arr = text.split(" ");
        String actualCommand = arr[0];

        if (actualCommand.equals("/update_name") && validateUpdateName(text)) {
            //Update User name
            String name = arr[1];

            super.getPSQL().addNewUser(123,"test");

            message.setText(name);
            super.getBot().execute(message);
        } else if (actualCommand.equals("/update_dob") && validateUpdateDOB(text)) {
            //Update User dob
            String dob = arr[1];
            message.setText(dob);
            super.getBot().execute(message);
        } else if (actualCommand.equals("/update_desc")) {
            //Update User desc
        } else if (actualCommand.equals("/update")) {
            //Update name and dob tgt
        } else {
            message.setText("Sorry, command is correct but input arguments are incorrect.");
            super.getBot().execute(message);
        }
    }

    private Boolean validateUpdateDOB(String text) {
        String[] arr = text.split(" ");

        return arr.length == 2 && validateDate(arr[1]);
    }

    private Boolean validateUpdateName(String text) {
        String[] arr = text.split(" ");

        return arr.length == 2;
    }
}
