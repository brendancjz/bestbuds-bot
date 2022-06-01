package Command.UserCommand;

import Command.Command;
import PSQL.PSQL;
import TelegramBot.BestBudsBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.Arrays;

public class UpdateCommand extends Command {

    public UpdateCommand(BestBudsBot bot, Update update, PSQL psql) throws URISyntaxException, SQLException {
        super(bot, update, psql);
    }

    @Override
    public void runCommand() {
        try {
            System.out.println("UpdateCommand.runCommand()");
            String text = super.getUpdate().getMessage().getText().trim();

            SendMessage message = new SendMessage();
            message.setChatId(super.getChatId().toString());
            message.enableHtml(true);

            if (text.equals("/update") || text.equals("/update_name") ||
                    text.equals("/update_dob") || text.equals("/update_desc")) { //Bad command
                missingArgumentsMessage(message);
                return;
            }

            runActualCommand(message, text);
        } catch (TelegramApiException | SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private void runActualCommand(SendMessage message, String text) throws TelegramApiException, SQLException {
        String[] arr = text.split(" ");
        String actualCommand = arr[0];

        if (actualCommand.equals("/update_name")) {
            //Update User name
            String name = String.join(" ", Arrays.copyOfRange(arr, 1, arr.length));

            if (validateUpdateName(text)) {
                super.getPSQL().updateUserName(super.getChatId(), name);

                message.setText("Successfully updated your name: " + name + ".");
            }

            super.getBot().execute(message);

        } else if (actualCommand.equals("/update_dob")) {
            //Update User dob
            String dob = arr[1];

            if (validateUpdateDOB(text)) {
                super.getPSQL().updateUserDOB(super.getChatId(), dob);

                message.setText("Successfully updated your date of birth: " + dob + ".");
            } else {
                message.setText("Sorry, inputted wrong format. Please input in this format: yyyy-MM-dd.");
            }

            super.getBot().execute(message);

        } else if (actualCommand.equals("/update_desc")) {
            //Update User desc
            String desc = String.join(" ", Arrays.copyOfRange(arr, 1, arr.length));

            if (validateDesc(text)) {
                super.getPSQL().updateUserDesc(super.getChatId(), desc);

                message.setText("Successfully updated your description: " + desc + ".");
            }

            super.getBot().execute(message);
        } else if (actualCommand.equals("/update")) {
            //Update name and dob tgt
            String name = arr[1];
            String dob = arr[2];

            if (validateUpdateNameAndDOB(text)) {
                super.getPSQL().updateUserNameAndDOB(super.getChatId(), name, dob);

                message.setText("Successfully updated your name and dob: " + name + ", " + dob + ".");
            } else {
                message.setText("Sorry, command is correct but input arguments are incorrect.");
            }
            super.getBot().execute(message);

        } else {
            message.setText("Sorry, command is correct but input arguments are incorrect.");
            super.getBot().execute(message);
        }
    }

    private Boolean validateUpdateNameAndDOB(String text) {
        String[] arr = text.split(" ");

        return arr.length == 3 && validateDate(arr[2]);
    }

    private Boolean validateUpdateDOB(String text) {
        String[] arr = text.split(" ");

        return arr.length == 2 && validateDate(arr[1]);
    }

    private Boolean validateUpdateName(String text) {
        String[] arr = text.split(" ");

        return arr.length >= 2;
    }

    private Boolean validateDesc(String text) {
        String[] arr = text.split(" ");

        return arr.length >= 2;
    }
}
