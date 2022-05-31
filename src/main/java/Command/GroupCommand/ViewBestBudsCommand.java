package Command.GroupCommand;

import Command.Command;
import PSQL.PSQL;
import TelegramBot.BestBudsBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import resource.Entity.Group;
import resource.Entity.User;

import java.net.URISyntaxException;
import java.sql.SQLException;

public class ViewBestBudsCommand extends Command {
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

            if (text.equals("//view_bestbuds")) { //Bad command. Missing arguments
                missingArgumentsMessage(message);
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

    private Boolean validateGroupCode(String text, Integer chatId) throws SQLException {
        String[] arr = text.split(" ");
        System.out.println("Is arr length 2? " + (arr.length == 2));
        System.out.println("Is Group Code Unique? " + super.getPSQL().isGroupCodeUnique(arr[1]));

        return arr.length == 2 && !super.getPSQL().isGroupCodeUnique(arr[1]) && super.getPSQL().isUserInGroup(chatId, arr[1]);
    }

    private String generateBestBudsDetails(Group group) throws SQLException {
        String deeds = "";

        if (group != null) {
            deeds += "<b><u>Your BestBuds Group Details:</u></b>\n\n";
            deeds += "<em>Name:</em> " + group.name + "\n";
            deeds += "<em>Code:</em>  " + group.code + "\n";
            deeds += "<em>Created By:</em> " + group.createdBy + "\n";
            deeds += "<em>Created On:</em> " + group.getCreatedOn() + "\n\n";

            for (User user : group.users) {
                deeds += "<em>User:</em> " + user.name + "\n";
            }

        } else {
            deeds = "Missing profile details.";
        }

        return deeds;
    }
}
