package Command.GroupCommand;

import Command.Command;
import PSQL.PSQL;
import TelegramBot.BestBudsBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import resource.Entity.Group;
import resource.KeyboardMarkup;

import java.net.URISyntaxException;
import java.sql.SQLException;

public class ExitCommand extends Command {
    private static final String COMMAND = "exit";

    public ExitCommand(BestBudsBot bot, Update update, PSQL psql) throws URISyntaxException, SQLException {
        super(bot, update, psql);
    }

    @Override
    public void runCommand() {
        try {
            System.out.println("ExitCommand.runCommand()");
            String text = super.getUpdate().getMessage().getText().trim();

            SendMessage message = new SendMessage();
            message.setChatId(super.getChatId().toString());
            message.enableHtml(false);

            if (text.equals("/exit")) { //Bad command. Missing arguments
                missingArgumentsMessage(message);
                return;
            }

            if (validateGroupCodeAndUserInGroup(text, super.getChatId())) {
                String[] arr = text.split(" ");
                String groupCode = arr[1];

                message.setReplyMarkup(KeyboardMarkup.confirmationKB(COMMAND, groupCode));
                message.setText("Confirm exiting a BestBuds Group: " + groupCode + "?");
            } else {
                message.setText("Sorry, it seems like the group code does not exist or you did not join this group.");
            }

            super.getBot().execute(message);

        } catch (TelegramApiException | SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @Override
    public void runCallback() {
        try {
            System.out.println("ExitCommand.runCallback()");
            Integer messageId = super.getUpdate().getCallbackQuery().getMessage().getMessageId();
            String callData = super.getUpdate().getCallbackQuery().getData();

            EditMessageText newMessage = new EditMessageText();
            newMessage.setChatId(super.getChatId().toString());
            newMessage.setMessageId(messageId);
            newMessage.enableHtml(true);

            String confirmationResult = callData.split("_")[2];
            String groupCode = callData.split("_")[3];

            if (confirmationResult.equals("YES")) {
                super.getPSQL().removeUserFromGroup(super.getChatId(), groupCode);
                Group group = super.getPSQL().getGroupDataResultSet(groupCode);
                newMessage.setText("You have exited the BestBuds Group: " + group.name);
                super.getBot().execute(newMessage);

            } else { //NO
                newMessage.setText("Cancelled BestBuds Group exit.");
                super.getBot().execute(newMessage);

            }

            super.getBot().execute(newMessage);
        } catch (TelegramApiException | SQLException e) {
            e.printStackTrace();
        }
    }

}
