package Command.GroupCommand;

import Command.Command;
import PSQL.PSQL;
import TelegramBot.BestBudsBot;
import Timer.BirthdayCheckerTimer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import resource.Entity.Group;
import resource.Entity.Message;
import resource.Entity.User;
import resource.KeyboardMarkup;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ViewBestBudsMessagesCommand extends Command {

    public ViewBestBudsMessagesCommand(BestBudsBot bot, Update update, PSQL psql) throws URISyntaxException, SQLException {
        super(bot, update, psql);
    }

    @Override
    public void runCommand() {
        try {
            System.out.println("ViewBestBudsMessagesCommand.runCommand()");
            String text = super.getUpdate().getMessage().getText().trim();

            if (validateGroupCodeAndUserAndAdminInGroup(text, super.getChatId())) {
                String[] arr = text.split(" ");
                String groupCode = arr[1];
                String userCode = arr[2];
                Group group = super.getPSQL().getGroupDataResultSet(groupCode);
                User user = super.getPSQL().getUserDataResultSet(userCode);
                User amin = super.getPSQL().getUserDataResultSet(super.getChatId());
                BirthdayCheckerTimer timer = new BirthdayCheckerTimer(super.getBot());
                List<Message> msges = super.getPSQL().getUserMessagesFromUsersOfGroup(user.code, group.code);
                timer.runSendMessageToAdminEvent(amin, user, group, msges);
            } else {
                SendMessage message = new SendMessage();
                message.setChatId(super.getChatId().toString());
                message.enableHtml(true);
                message.setText("Sorry, it seems like the group code does not exist or you are not an admin or BestBud is not in group.");
                super.getBot().execute(message);
            }
        } catch (URISyntaxException | SQLException | TelegramApiException e) {
            e.printStackTrace();
        }

    }

    private Boolean validateGroupCodeAndUserAndAdminInGroup(String text, Integer chatId) throws SQLException {
        String[] arr = text.split(" ");

        return arr.length == 3 &&
                !super.getPSQL().isGroupCodeUnique(arr[1]) &&
                super.getPSQL().isUserInGroup(arr[2], arr[1]) &&
                super.getPSQL().isAdminInGroup(chatId, arr[1]);
    }
}
