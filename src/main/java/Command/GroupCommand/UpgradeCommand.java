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
            System.out.println("CreateCommand.runCommand()");
            String text = super.getUpdate().getMessage().getText().trim();

            SendMessage message = new SendMessage();
            message.setChatId(super.getChatId().toString());
            message.enableHtml(false);

            if (text.equals("/" + COMMAND)) { //Bad command. Missing arguments
                missingArgumentsMessage(message);
                return;
            }

            if (validate(text)) {
                String[] arr = text.split(" ");
                String userCode = arr[1];
                String groupCode = arr[2];

                User user = super.getPSQL().getUserDataResultSet(userCode);
                Group group = super.getPSQL().getGroupDataResultSet(groupCode);

                super.getPSQL().makeUserAdminInGroup(user.chatId, group.code);
                message.setText("Upgraded " + user.name + " to an Administrator for " + group.name + "?");
            } else {
                message.setText("Something went wrong. Please contact developer.");
            }

            super.getBot().execute(message);
        } catch (TelegramApiException | SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private Boolean validate(String text) throws SQLException {
        String[] arr = text.split(" ");

        return arr.length == 3 &&
                !User.isNull(super.getPSQL().getUserDataResultSet(arr[1])) &&
                super.getPSQL().isUserInGroup(super.getPSQL().getUserDataResultSet(arr[1]).chatId, arr[2]);
    }
}
