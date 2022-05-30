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

    public CreateCommand(BestBudsBot bot, Update update, PSQL psql) throws URISyntaxException, SQLException {
        super(bot, update, psql);

    }

    @Override
    public void runCommand() {
        try {
            System.out.println("CreateCommand.runCommand()");
            String text = super.getUpdate().getMessage().getText();

            SendMessage message = new SendMessage();
            message.setChatId(super.getChatId().toString());
            message.enableHtml(false);

            if (text.equals("/create")) { //Bad command. Missing arguments
                missingArgumentsMessage(message);
                return;
            }

            if (validateGroupName(text)) {
                String[] arr = text.split(" ");
                String groupName = String.join(" ", Arrays.copyOfRange(arr, 1, arr.length));

                message.setReplyMarkup(KeyboardMarkup.confirmationKB(groupName));
                message.setText("Confirm creating a BestBuds Group: " + groupName + "?");
            }

            //TODO Use Callback to confirm the group name and creation of group
            //TODO add this new group into the db
            //TODO add the user into this db. If user, not register, register the man in there.

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
            String firstName = super.getUpdate().getCallbackQuery().getMessage().getChat().getFirstName();
            String callData = super.getUpdate().getCallbackQuery().getData();

            EditMessageText newMessage = new EditMessageText();
            newMessage.setChatId(super.getChatId().toString());
            newMessage.setMessageId(messageId);
            newMessage.enableHtml(true);

            String confirmationResult = callData.split("_")[1];
            String groupName = callData.split("_")[2];

            if (confirmationResult.equals("YES")) {
                Group newGroup = super.getPSQL().addNewGroup(super.getChatId(), groupName);
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

        return arr.length >= 2;
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
