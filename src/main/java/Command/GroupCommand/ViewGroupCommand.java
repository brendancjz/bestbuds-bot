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
import java.util.ArrayList;
import java.util.List;

public class ViewGroupCommand extends Command {
    private static final String COMMAND = "viewGroup";

    public ViewGroupCommand(BestBudsBot bot, Update update, PSQL psql) throws URISyntaxException, SQLException {
        super(bot, update, psql);
    }

    @Override
    public void runCommand() {
        try {
            System.out.println("ViewGroupCommand.runCommand()");
            String text = super.getUpdate().getMessage().getText().trim();

            SendMessage message = new SendMessage();
            message.setChatId(super.getChatId().toString());
            message.enableHtml(true);

            if (text.equals("/view_group")) { //Bad command. Missing arguments
                List<Group> groups = super.getPSQL().getGroupsFromUser(super.getChatId());

                message.setText(generateGroupSelection(groups));

                List<String> groupNames = new ArrayList<>();
                for (Group group : groups) {
                    groupNames.add(group.name);
                }
                message.setReplyMarkup(KeyboardMarkup.selectKB(groupNames, COMMAND));
                super.getBot().execute(message);
                return;
            }

            if (validateGroupCodeAndUserInGroup(text, super.getChatId())) {
                String[] arr = text.split(" ");
                String groupCode = arr[1];
                Group group = super.getPSQL().getGroupDataResultSet(groupCode);
                message.setText(this.generateGroupDetails(group));
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
            System.out.println("ViewGroupCommand.runCallback()");
            Integer messageId = super.getUpdate().getCallbackQuery().getMessage().getMessageId();
            String callData = super.getUpdate().getCallbackQuery().getData();

            EditMessageText newMessage = new EditMessageText();
            newMessage.setChatId(super.getChatId().toString());
            newMessage.setMessageId(messageId);
            newMessage.enableHtml(true);

            String groupSelection = callData.split("_")[2];

            Group group = super.getPSQL().getGroupDataResultSet(groupSelection);

            newMessage.setText(this.generateGroupDetails(group));

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
            deeds += "<em>Population:</em> " + group.users.size() + " BestBuds\n";
            deeds += "<em>Description:</em> " + group.description + "\n";


        } else {
            deeds = "Missing profile details.";
        }

        return deeds;
    }

    private String generateGroupSelection(List<Group> groups) {
        String msg = "";
        if (groups.size() > 0) {
            msg = "<b>Select Group to View Details</b>";
        } else {
            msg = "You have not joined any BestBuds Group.";
        }

        return msg;
    }
}
