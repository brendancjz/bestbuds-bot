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

public class JoinCommand extends Command {
    private static final String COMMAND = "join";

    public JoinCommand(BestBudsBot bot, Update update, PSQL psql) throws URISyntaxException, SQLException {
        super(bot, update, psql);
    }

    @Override
    public void runCommand() {
        try {
            System.out.println("JoinCommand.runCommand()");
            String text = super.getUpdate().getMessage().getText().trim();

            SendMessage message = new SendMessage();
            message.setChatId(super.getChatId().toString());
            message.enableHtml(false);

            if (text.equals("/join")) { //Bad command. Missing arguments
                missingArgumentsMessage(message);
                return;
            }

            if (validateGroupCode(text)) {
                String[] arr = text.split(" ");
                String groupCode = arr[1];

                message.setReplyMarkup(KeyboardMarkup.confirmationKB(COMMAND, groupCode));
                message.setText("Confirm joining a BestBuds Group: " + groupCode + "?");
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
            System.out.println("JoinCommand.runCallback()");
            Integer messageId = super.getUpdate().getCallbackQuery().getMessage().getMessageId();
            String callData = super.getUpdate().getCallbackQuery().getData();

            EditMessageText newMessage = new EditMessageText();
            newMessage.setChatId(super.getChatId().toString());
            newMessage.setMessageId(messageId);
            newMessage.enableHtml(true);

            String confirmationResult = callData.split("_")[2];
            String groupCode = callData.split("_")[3];

            if (confirmationResult.equals("YES")) {
                Group group = new Group();
                if (!super.getPSQL().isUserInGroup(super.getChatId(), groupCode) && super.getPSQL().addUserIntoGroup(super.getChatId(), groupCode)) {
                    group = super.getPSQL().getGroupDataResultSet(groupCode);
                    newMessage.setText("You have joined the BestBuds Group: " + group.name);
                    super.getBot().execute(newMessage);

                    SendMessage message2 = new SendMessage();
                    message2.setChatId(super.getChatId().toString());
                    message2.enableHtml(true);
                    message2.setText(generateGroupDetails(group));
                    super.getBot().execute(message2);
                } else {
                    newMessage.setText("Sorry, something went wrong. Please feedback this to the developer.");
                    super.getBot().execute(newMessage);
                }

            } else { //NO
                newMessage.setText("Cancelled BestBuds Group creation.");
                super.getBot().execute(newMessage);

            }

            super.getBot().execute(newMessage);
        } catch (TelegramApiException | SQLException e) {
            e.printStackTrace();
        }
    }

    private String generateGroupDetails(Group group) throws SQLException {
        String deeds = "";

        if (group != null) {
            deeds += "<b><u>Your BestBuds Group Details:</u></b>\n\n";
            deeds += "<em>Name:</em> " + group.name + "\n";
            deeds += "<em>Code:</em>  " + group.code + "\n";
            deeds += "<em>Created By:</em> " + group.createdBy + "\n";
            deeds += "<em>Created On:</em> " + group.getCreatedOn() + "\n";
            deeds += "<em>Description:</em> " + group.description + "\n";

        } else {
            deeds = "Missing group details.";
        }

        return deeds;
    }
}
