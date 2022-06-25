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
    private static final Integer NUM_OF_USERS_PER_PAGE = 3;
    private static final Integer FIRST_PAGE = 1;
    private static final String COMMAND = "viewBestBuds";

    public ViewBestBudsCommand(BestBudsBot bot, Update update, PSQL psql) throws URISyntaxException, SQLException {
        super(bot, update, psql);
    }

    @Override
    public void runCommand() {
        try {
            System.out.println("ViewBestBudsCommand.runCommand()");
            String text = super.getUpdate().getMessage().getText().trim();

            SendMessage message = new SendMessage();
            message.setChatId(super.getChatId().toString());
            message.enableHtml(true);

            if (text.equals("/view_bestbuds")) {
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
                message.setText(this.generateBestBudsDetails(group, FIRST_PAGE));
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
            String callData = super.getUpdate().getCallbackQuery().getData();

            EditMessageText newMessage = new EditMessageText();
            newMessage.setChatId(super.getChatId().toString());
            newMessage.setMessageId(messageId);
            newMessage.enableHtml(true);

            String callbackData = callData.split("_")[2];
            String groupSelection;
            if (!Group.isNull(super.getPSQL().getGroupDataResultSet(callbackData))) {
                //First page after selecting the group to view the bestbuds OR Refreshing content
                groupSelection = callbackData;
                Group group = super.getPSQL().getGroupDataResultSet(groupSelection);
                newMessage.setText(this.generateBestBudsDetails(group, FIRST_PAGE));

                if (group.users.size() > NUM_OF_USERS_PER_PAGE) {
                    newMessage.setReplyMarkup(KeyboardMarkup.refreshNavigationKB(groupSelection, COMMAND, FIRST_PAGE, false));
                } else {
                    newMessage.setReplyMarkup(KeyboardMarkup.refreshNavigationKB(groupSelection, COMMAND, FIRST_PAGE, true));

                }
            } else {
                //This should be the callback from navigating
                Integer pageNo = Integer.valueOf(callData.split("_")[2]);
                groupSelection = callData.split("_")[3];
                Group group = super.getPSQL().getGroupDataResultSet(groupSelection);
                newMessage.setText(this.generateBestBudsDetails(group, pageNo));
                if (pageNo * NUM_OF_USERS_PER_PAGE >= group.users.size()) {
                    newMessage.setReplyMarkup(KeyboardMarkup.refreshNavigationKB(groupSelection, COMMAND, pageNo, true));
                } else {
                    newMessage.setReplyMarkup(KeyboardMarkup.refreshNavigationKB(groupSelection, COMMAND, pageNo, false));
                }
            }

            super.getBot().execute(newMessage);
        } catch (TelegramApiException | SQLException e) {
            e.printStackTrace();
        }
    }

    private String generateBestBudsDetails(Group group, Integer pageNo) throws SQLException {
        StringBuilder deeds = new StringBuilder();

        if (group != null) {
            if (pageNo == 1) {
                deeds.append("<b><u>Your BestBuds Group Details:</u></b>\n\n");
                deeds.append("<em>Name:</em> ").append(group.name).append("\n");
                deeds.append("<em>Code:</em>  ").append(group.code).append("\n");
                deeds.append("<em>Created By:</em> ").append(group.createdBy).append("\n");
                deeds.append("<em>Created On:</em> ").append(group.getCreatedOn()).append("\n");
                deeds.append("<em>Description:</em> ").append(group.description).append("\n\n");
            }

            deeds.append("<b><u>BestBuds Details: </u></b>\n");
            int count = 0;
            for (User user : group.users) {
                count++;
                //Only a certain number of the users will be shown
                if (count <= (pageNo * NUM_OF_USERS_PER_PAGE) &&
                        count > ((pageNo - 1) * NUM_OF_USERS_PER_PAGE)) {
                    deeds.append(generateProfileDetails(user));
                }
            }

        } else {
            deeds = new StringBuilder("Missing group details.");
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
        String msg = "";
        if (groups.size() > 0) {
            msg = "<b>Select Group to View BestBuds</b>";
        } else {
            msg = "You have not joined any BestBuds Group.";
        }

        return msg;
    }
}
