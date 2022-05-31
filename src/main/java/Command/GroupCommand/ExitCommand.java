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
            String text = super.getUpdate().getMessage().getText();

            SendMessage message = new SendMessage();
            message.setChatId(super.getChatId().toString());
            message.enableHtml(false);

            if (text.equals("/exit")) { //Bad command. Missing arguments
                missingArgumentsMessage(message);
                return;
            }

            if (validateGroupCode(text)) {
                String[] arr = text.split(" ");
                String groupCode = arr[1];

                message.setReplyMarkup(KeyboardMarkup.confirmationKB(COMMAND, groupCode));
                message.setText("Confirm exiting a BestBuds Group: " + groupCode + "?");
            } else {
                message.setText("Sorry, it seems like the group code does not exist.");
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
            String firstName = super.getUpdate().getCallbackQuery().getMessage().getChat().getFirstName();
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

                SendMessage message2 = new SendMessage();
                message2.setChatId(super.getChatId().toString());
                message2.enableHtml(true);
                message2.setText(generateGroupDetails(group));
                super.getBot().execute(message2);

            } else { //NO
                newMessage.setText("Cancelled BestBuds Group exit.");
                super.getBot().execute(newMessage);

            }

            super.getBot().execute(newMessage);
        } catch (TelegramApiException | SQLException e) {
            e.printStackTrace();
        }
    }

    private Boolean validateGroupCode(String text) throws SQLException {
        String[] arr = text.split(" ");
        System.out.println("Is arr length 2? " + (arr.length == 2));
        System.out.println("Is Group Code Unique? " + super.getPSQL().isGroupCodeUnique(arr[1]));

        return arr.length == 2 && !super.getPSQL().isGroupCodeUnique(arr[1]);
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
