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
import java.util.ArrayList;
import java.util.List;

public class ViewBestBudsCommand extends Command {
    private static final String COMMAND = "viewBestBuds";

    public ViewBestBudsCommand(BestBudsBot bot, Update update, PSQL psql) throws URISyntaxException, SQLException {
        super(bot, update, psql);
    }

    @Override
    public void runCommand() {
        try {
            System.out.println("ViewBestBudsCommand.runCommand()");
            String text = super.getUpdate().getMessage().getText();

            SendMessage message = new SendMessage();
            message.setChatId(super.getChatId().toString());
            message.enableHtml(true);

            if (text.equals("/view_bestbuds")) { //TODO allow user to choose which group he wants to see
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

            if (validateGroupCode(text, super.getChatId())) {
                String[] arr = text.split(" ");
                String groupCode = arr[1];

                Group group = super.getPSQL().getGroupDataResultSet(groupCode);

                message.setText(this.generateBestBudsDetails(group));

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
            System.out.println("ViewBestBudsCommand.runCallback()");
            Integer messageId = super.getUpdate().getCallbackQuery().getMessage().getMessageId();
            String firstName = super.getUpdate().getCallbackQuery().getMessage().getChat().getFirstName();
            String callData = super.getUpdate().getCallbackQuery().getData();

            EditMessageText newMessage = new EditMessageText();
            newMessage.setChatId(super.getChatId().toString());
            newMessage.setMessageId(messageId);
            newMessage.enableHtml(true);

            String groupSelection = callData.split("_")[2];
            newMessage.setText(groupSelection);

//            if (groupSelection.equals("YES")) {
//                super.getPSQL().removeUserFromGroup(super.getChatId(), groupCode);
//                Group group = super.getPSQL().getGroupDataResultSet(groupCode);
//                newMessage.setText("You have exited the BestBuds Group: " + group.name);
//                super.getBot().execute(newMessage);
//
//            } else { //NO
//                newMessage.setText("Cancelled BestBuds Group exit.");
//                super.getBot().execute(newMessage);
//
//            }

            super.getBot().execute(newMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private Boolean validateGroupCode(String text, Integer chatId) throws SQLException {
        String[] arr = text.split(" ");
        System.out.println("Is arr length 2? " + (arr.length == 2));
        System.out.println("Is Group Code Unique? " + super.getPSQL().isGroupCodeUnique(arr[1]));

        return arr.length == 2 && !super.getPSQL().isGroupCodeUnique(arr[1]) && super.getPSQL().isUserInGroup(chatId, arr[1]);
    }

    private String generateBestBudsDetails(Group group) throws SQLException {
        StringBuilder deeds = new StringBuilder();

        if (group != null) {
            deeds.append("<b><u>Your BestBuds Group Details:</u></b>\n\n");
            deeds.append("<em>Name:</em> ").append(group.name).append("\n");
            deeds.append("<em>Code:</em>  ").append(group.code).append("\n");
            deeds.append("<em>Created By:</em> ").append(group.createdBy).append("\n");
            deeds.append("<em>Created On:</em> ").append(group.getCreatedOn()).append("\n\n");

            deeds.append("<b><u>BestBuds Details: </u></b>\n");
            for (User user : group.users) {
                deeds.append(generateProfileDetails(user));
            }

        } else {
            deeds = new StringBuilder("Missing profile details.");
        }

        return deeds.toString();
    }

    private String generateProfileDetails(User user) {
        String deeds = "";

        deeds += "<em>Name:</em> " + user.name + "\n";
        deeds += "<em>Code:</em>  " + user.code + "\n";
        deeds += "<em>D.O.B:</em> " + user.getDob() + "\n";
        deeds += "<em>Description:</em> " + user.desc + "\n\n";


        return deeds;
    }

    private String generateGroupSelection(List<Group> groups) {
        String msg = "<b>Select Group to View BestBuds</b>";

        return msg;
    }
}
