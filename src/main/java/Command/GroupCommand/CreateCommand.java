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
import java.util.Arrays;

public class CreateCommand extends Command {
    private static final String COMMAND = "create";

    public CreateCommand(BestBudsBot bot, Update update, PSQL psql) throws URISyntaxException, SQLException {
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

            if (validateGroupName(text)) {
                String[] arr = text.split(" ");
                String groupName = String.join(" ", Arrays.copyOfRange(arr, 1, arr.length));

                message.setReplyMarkup(KeyboardMarkup.confirmationKB(COMMAND, groupName));
                message.setText("Confirm creating a BestBuds Group: " + groupName);
            } else {
                message.setText("Something went wrong. Please contact developer.");
            }

            super.getBot().execute(message);
        } catch (TelegramApiException throwables) {
            throwables.printStackTrace();
        }
    }

    @Override
    public void runCallback() {
        try {
            System.out.println("CreateCommand.runCallback()");
            Integer messageId = super.getUpdate().getCallbackQuery().getMessage().getMessageId();
            String callData = super.getUpdate().getCallbackQuery().getData();

            EditMessageText newMessage = new EditMessageText();
            newMessage.setChatId(super.getChatId().toString());
            newMessage.setMessageId(messageId);
            newMessage.enableHtml(true);

            String confirmationResult = callData.split("_")[2];
            String groupName = callData.split("_")[3];

            if (confirmationResult.equals("YES")) {
                Group newGroup = super.getPSQL().addNewGroup(super.getChatId(), groupName);
                super.getPSQL().addUserIntoGroup(super.getChatId(), newGroup.code);
                super.getPSQL().makeUserAdminInGroup(super.getChatId(), newGroup.code);
                newMessage.setText("You are the owner of a new BestBuds Group: " + groupName);
                super.getBot().execute(newMessage);

                SendMessage message2 = new SendMessage();
                message2.setChatId(super.getChatId().toString());
                message2.enableHtml(true);
                message2.setText(generateGroupDetails(newGroup));
                super.getBot().execute(message2);

            } else { //NO
                newMessage.setText("Cancelled BestBuds Group creation.");
                super.getBot().execute(newMessage);
            }

            super.getBot().execute(newMessage);
        } catch (TelegramApiException | SQLException e) {
            e.printStackTrace();
        }
    }

    private Boolean validateGroupName(String text) {
        String[] arr = text.split(" ");

        return arr.length >= 2 && !text.contains("<") && !text.contains(">");
    }

    private String generateGroupDetails(Group group) throws SQLException {
        String deeds = "";

        if (group != null) {
            deeds += "<b><u>Your BestBuds Group Details:</u></b>\n\n";
            deeds += "<em>Name:</em> " + group.name + "\n";
            deeds += "<em>Code:</em>  " + group.code + "\n";
            deeds += "<em>Created By:</em> " + group.createdBy + "\n";
            deeds += "<em>Created On:</em> " + group.getCreatedOn() + "\n";

        } else {
            deeds = "Missing profile details.";
        }

        return deeds;
    }
}
