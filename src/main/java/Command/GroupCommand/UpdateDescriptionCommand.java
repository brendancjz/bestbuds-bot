package Command.GroupCommand;

import Command.Command;
import PSQL.PSQL;
import TelegramBot.BestBudsBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import resource.Entity.Group;

import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.Arrays;

public class UpdateDescriptionCommand extends Command {
    public UpdateDescriptionCommand(BestBudsBot bot, Update update, PSQL psql) throws URISyntaxException, SQLException {
        super(bot, update, psql);
    }

    @Override
    public void runCommand() {
        try {
            System.out.println("UpdateDescriptionCommand.runCommand()");
            String text = super.getUpdate().getMessage().getText().trim();

            SendMessage message = new SendMessage();
            message.setChatId(super.getChatId().toString());
            message.enableHtml(true);

            if (text.equals("/update_group_desc")) { //Bad command
                missingArgumentsMessage(message);
                return;
            }

            String[] arr = text.split(" ");
            //Update User desc
            String groupCode = arr[1];
            String desc = String.join(" ", Arrays.copyOfRange(arr, 2, arr.length));

            if (validateDesc(text) && super.getPSQL().isUserInGroup(super.getChatId(),groupCode)) {
                super.getPSQL().updateGroupDesc(groupCode, desc);
                Group updatedGroup = super.getPSQL().getGroupDataResultSet(groupCode);
                message.setText("Successfully updated <em>" + updatedGroup.name + "</em> description:\n" + desc);
            } else {
                message.setText("Sorry, incorrect formatting or group does not exist. Please try again.");
            }

            super.getBot().execute(message);
        } catch (TelegramApiException | SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private Boolean validateDesc(String text) {
        String[] arr = text.split(" ");

        return arr.length >= 2;
    }
}
