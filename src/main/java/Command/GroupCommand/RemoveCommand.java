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

public class RemoveCommand extends Command {
    private static final String COMMAND = "remove";
    public RemoveCommand(BestBudsBot bot, Update update, PSQL psql) throws URISyntaxException, SQLException {
        super(bot, update, psql);
    }

    @Override
    public void runCommand() {
        try {
            System.out.println("RemoveCommand.runCommand()");
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

                if (super.getPSQL().isUserOwnerOfGroup(super.getChatId(), groupCode)) {
                    User user = super.getPSQL().getUserDataResultSet(userCode);
                    Group group = super.getPSQL().getGroupDataResultSet(groupCode);

                    String callData = "" + user.chatId + "_" + group.code;
                    message.setReplyMarkup(KeyboardMarkup.confirmationKB(COMMAND, callData));
                    message.setText("Confirm removing " + user.name + " from " + group.name);
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

    @Override
    public void runCallback() {
        try {
            System.out.println("RemoveCommand.runCallback()");
            Integer messageId = super.getUpdate().getCallbackQuery().getMessage().getMessageId();
            String firstName = super.getUpdate().getCallbackQuery().getMessage().getChat().getFirstName();
            String callData = super.getUpdate().getCallbackQuery().getData();

            EditMessageText newMessage = new EditMessageText();
            newMessage.setChatId(super.getChatId().toString());
            newMessage.setMessageId(messageId);
            newMessage.enableHtml(true);

            String confirmationResult = callData.split("_")[2];
            String chatId = callData.split("_")[3];
            String groupCode = callData.split("_")[4];

            if (confirmationResult.equals("YES")) {
                User user = super.getPSQL().getUserDataResultSet(Integer.parseInt(chatId));
                Group group = super.getPSQL().getGroupDataResultSet(groupCode);

                super.getPSQL().removeUserFromGroup(Integer.parseInt(chatId), groupCode);

                newMessage.setText("You have removed " + user.name + " from " + group.name);
                super.getBot().execute(newMessage);

                SendMessage message2 = new SendMessage();
                message2.setChatId(chatId);
                message2.enableHtml(true);
                message2.setText("Hello " + user.name + ", you have been removed from " + group.name);
                super.getBot().execute(message2);

            } else { //NO
                newMessage.setText("Cancelled BestBud removal.");
                super.getBot().execute(newMessage);

            }

            super.getBot().execute(newMessage);
        } catch (TelegramApiException | SQLException e) {
            e.printStackTrace();
        }
    }

    private Boolean validate(String text) throws SQLException {
        String[] arr = text.split(" ");

        return arr.length == 3 &&
                !User.isNull(super.getPSQL().getUserDataResultSet(arr[1])) &&
                super.getPSQL().isUserInGroup(super.getPSQL().getUserDataResultSet(arr[1]).chatId, arr[2]);
    }
}
