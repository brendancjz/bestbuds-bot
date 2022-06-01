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

public class ShareCodeCommand extends Command {
    private static final String COMMAND = "shareCode";

    public ShareCodeCommand(BestBudsBot bot, Update update, PSQL psql) throws URISyntaxException, SQLException {
        super(bot, update, psql);
    }

    @Override
    public void runCommand() {
        try {
            System.out.println("ShareCodeCommand.runCommand()");
            String text = super.getUpdate().getMessage().getText();

            SendMessage message = new SendMessage();
            message.setChatId(super.getChatId().toString());
            message.enableHtml(true);

            if (text.equals("/share_code")) { //Bad command. Missing arguments
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

                message.setText(this.generateShareCodeMessage(group));
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
            System.out.println("ShareCodeCommand.runCallback()");
            Integer messageId = super.getUpdate().getCallbackQuery().getMessage().getMessageId();
            String firstName = super.getUpdate().getCallbackQuery().getMessage().getChat().getFirstName();
            String callData = super.getUpdate().getCallbackQuery().getData();

            EditMessageText newMessage = new EditMessageText();
            newMessage.setChatId(super.getChatId().toString());
            newMessage.setMessageId(messageId);
            newMessage.enableHtml(true);

            String groupSelection = callData.split("_")[2];

            Group group = super.getPSQL().getGroupDataResultSet(groupSelection);

            newMessage.setText(this.generateShareCodeMessage(group));

            super.getBot().execute(newMessage);
        } catch (TelegramApiException | SQLException e) {
            e.printStackTrace();
        }
    }

    private Boolean validateGroupCode(String text, Integer chatId) throws SQLException {
        String[] arr = text.split(" ");
        System.out.println("Is arr length 2? " + (arr.length == 2));
        System.out.println("Is Group Code Unique? " + super.getPSQL().isGroupCodeUnique(arr[1]));

        return arr.length == 2 && !super.getPSQL().isGroupCodeUnique(arr[1]) && super.getPSQL().isUserInGroup(chatId, arr[1]);
    }

    private String generateShareCodeMessage(Group group) {
        String deeds = "";

        if (group != null) {
            deeds += "Hello, come join my BestBuds Group <em>" + group.name + "</em>!\n";
            deeds += "Start the bot @bbb_bestbuds_bot and use this command: <pre>  /join " + group.code + "</pre>";

        } else {
            deeds = "Sorry, no code to share.";
        }

        return deeds;
    }

    private String generateGroupSelection(List<Group> groups) {
        String msg = "";
        if (groups.size() > 0) {
            msg = "<b>Select Group to Share Code</b>";
        } else {
            msg = "You have not joined any BestBuds Group.";
        }

        return msg;
    }
}
