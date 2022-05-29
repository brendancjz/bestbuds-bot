package Command.GroupCommand;

import Command.Command;
import PSQL.PSQL;
import TelegramBot.BestBudsBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import resource.KeyboardMarkup;

import java.net.URISyntaxException;
import java.sql.SQLException;
import java.text.ParseException;
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
            Integer chatId = Integer.parseInt(super.getUpdate().getCallbackQuery().getMessage().getChatId().toString());

            EditMessageText newMessage = new EditMessageText();
            newMessage.setChatId(chatId.toString());
            newMessage.setMessageId(messageId);
            newMessage.enableHtml(true);

            String confirmationResult = callData.split("_")[1];
            String groupName = callData.split("_")[2];

            if (confirmationResult.equals("YES")) {
                newMessage.setText("You are the owner of a new BestBuds Group: " + groupName);
                super.getPSQL().addNewGroup(chatId, groupName);
            } else { //NO
                newMessage.setText("Cancelled BestBuds Group creation.");
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
}
