package Command.GroupCommand;

import Command.Command;
import PSQL.PSQL;
import TelegramBot.BestBudsBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import resource.Entity.Group;
import resource.Entity.User;
import resource.KeyboardMarkup;

import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.Arrays;

public class UpgradeCommand extends Command {
    private static final String COMMAND = "upgrade";
    public UpgradeCommand(BestBudsBot bot, Update update, PSQL psql) throws URISyntaxException, SQLException {
        super(bot, update, psql);
    }

    @Override
    public void runCommand() {
        try {
            System.out.println("UpgradeCommand.runCommand()");
            String text = super.getUpdate().getMessage().getText().trim();

            SendMessage message = new SendMessage();
            message.setChatId(super.getChatId().toString());
            message.enableHtml(false);

            if (text.equals("/" + COMMAND)) { //Bad command. Missing arguments
                missingArgumentsMessage(message);
                return;
            }

            if (validateUserCodeGroupCodeAndUserInGroup(text)) {
                String[] arr = text.split(" ");
                String userCode = arr[1];
                String groupCode = arr[2];

                if (super.getPSQL().isUserOwnerOfGroup(super.getChatId(), groupCode)) {
                    User user = super.getPSQL().getUserDataResultSet(userCode);
                    Group group = super.getPSQL().getGroupDataResultSet(groupCode);

                    super.getPSQL().makeUserAdminInGroup(user.chatId, group.code);
                    message.setText("Upgraded " + user.name + " to an Administrator for " + group.name);
                } else {
                    message.setText("Sorry, you're not the owner of the group.");
                }
            } else {
                message.setText("Something went wrong. Please contact developer.");
            }

            super.getBot().execute(message);
        } catch (TelegramApiException | SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}
