package Command.GroupCommand;

import Command.Command;
import PSQL.PSQL;
import TelegramBot.BestBudsBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import resource.Entity.Group;
import resource.Entity.User;

import java.net.URISyntaxException;
import java.sql.SQLException;

public class DowngradeCommand extends Command {
    private static final String COMMAND = "downgrade";
    public DowngradeCommand(BestBudsBot bot, Update update, PSQL psql) throws URISyntaxException, SQLException {
        super(bot, update, psql);
    }

    @Override
    public void runCommand() {
        try {
            System.out.println("DowngradeCommand.runCommand()");
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

                super.getPSQL().makeUserNormalInGroup(user.chatId, group.code);
                message.setText("Downgraded " + user.name + " to a normal BestBud for " + group.name);
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
