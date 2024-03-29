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

public class SubscribeCommand extends Command {

    private static final Integer NUM_OF_PAGES = 2;
    private static final Integer FIRST_PAGE = 1;
    private static final String COMMAND = "subscribe";

    public SubscribeCommand(BestBudsBot bot, Update update, PSQL psql) throws URISyntaxException, SQLException {
        super(bot, update, psql);

    }

    @Override
    public void runCommand() {
        try {
            System.out.println("SubscribeCommand.runCommand()");
            String text = super.getUpdate().getMessage().getText().trim();

            SendMessage message = new SendMessage();
            message.setChatId(super.getChatId().toString());
            message.enableHtml(true);

            if (NUM_OF_PAGES != FIRST_PAGE) message.setReplyMarkup(KeyboardMarkup.continueKB(COMMAND));

            if (text.equals("/" + COMMAND)) { //Instructions to create or join a Group
                message.setText(generateSubscribeInstruction(FIRST_PAGE));
                super.getBot().execute(message);

            } else {
                invalidMessage(message, text);
            }
        } catch (TelegramApiException throwables) {
            throwables.printStackTrace();
        }
    }

    @Override
    public void runCallback() {
        try {
            System.out.println("SubscribeCommand.runCallback()");
            Integer messageId = super.getUpdate().getCallbackQuery().getMessage().getMessageId();
            String firstName = super.getUpdate().getCallbackQuery().getMessage().getChat().getFirstName();
            String callData = super.getUpdate().getCallbackQuery().getData();
            Integer chatId = Integer.parseInt(super.getUpdate().getCallbackQuery().getMessage().getChatId().toString());

            EditMessageText newMessage = new EditMessageText();
            newMessage.setChatId(chatId.toString());
            newMessage.setMessageId(messageId);
            newMessage.enableHtml(true);

            Integer currentPageNumber = Integer.parseInt(callData.split("_")[2]);
            System.out.println("Page " + currentPageNumber);

            setCorrectKeyboard(newMessage, currentPageNumber);

            if (currentPageNumber == 1) {
                newMessage.setText(generateSubscribeInstruction(currentPageNumber));
            } else if (currentPageNumber == 2) {
                newMessage.setText(generateCommandList(currentPageNumber));
            }

            super.getBot().execute(newMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void setCorrectKeyboard(EditMessageText newMessage, Integer pageNo) {
        if (NUM_OF_PAGES == FIRST_PAGE) {
            return;
        }

        if (pageNo == FIRST_PAGE) {
            newMessage.setReplyMarkup(KeyboardMarkup.continueKB(COMMAND));
        } else if (pageNo == NUM_OF_PAGES) {
            newMessage.setReplyMarkup(KeyboardMarkup.backKB(COMMAND, pageNo));
        } else {
            newMessage.setReplyMarkup(KeyboardMarkup.navigationKB(COMMAND, pageNo));
        }
    }

    private String generateCommandList(Integer pageNo) {
        String instruction = "";
        instruction += "<b>BestBuds Group Commands - Page " + pageNo + " out of " + NUM_OF_PAGES + "</b>\n";
        instruction += "<em>If you're on mobile, tap on the command to copy the text.</em>\n\n";
        instruction += "<em>Join a BestBuds Group</em>\n";
        instruction += "<pre>  /join &lt;group_code&gt;</pre>\n\n";
        instruction += "<em>Create a BestBuds Group</em>\n";
        instruction += "<pre>  /create &lt;group_name&gt;</pre>\n\n";
        instruction += "<em>Leave a BestBuds Group</em>\n";
        instruction += "<pre>  /exit &lt;group_code&gt;</pre>\n\n";
        instruction += "<em>View all BestBuds in BestBuds Group</em>\n";
        instruction += "<pre>  /view_bestbuds &lt;group_code&gt;[optional]</pre>\n\n";
        instruction += "<em>View BestBuds Group Details</em>\n";
        instruction += "<pre>  /view_group &lt;group_code&gt;[optional]</pre>\n\n";
        instruction += "<em>Share BestBuds Group Code</em>\n";
        instruction += "<pre>  /share_code &lt;group_code&gt;[optional]</pre>\n\n";
        //instruction += "<em>Update BestBuds Group Description</em>\n";
        //instruction += "<pre>  /update_group_desc &lt;group_code&gt;[optional]</pre>\n\n\n";
        instruction += "<b>Group Owner Commands</b> \n\n";
        instruction += "<em>Remove a BestBud</em>\n";
        instruction += "<pre>  /remove &lt;user_code&gt; &lt;group_code&gt;</pre>\n\n";
        instruction += "<em>Upgrade a BestBud to Admin</em>\n";
        instruction += "<pre>  /upgrade &lt;user_code&gt; &lt;group_code&gt;</pre>\n\n";
        instruction += "<em>Downgrade a BestBud to Admin</em>\n";
        instruction += "<pre>  /downgrade &lt;user_code&gt; &lt;group_code&gt;</pre>\n\n";
        instruction += "<b>Group Admin Commands</b> \n\n";
        instruction += "<em>View BestBud Messages During Birthday Week</em>\n";
        instruction += "<pre>  /view_bestbuds_messages &lt;group_code&gt; &lt;user_code&gt;</pre>\n\n";


        return instruction;
    }

    private String generateSubscribeInstruction(Integer pageNo) {
        String instruction = "";

        instruction += "<b>Subscribe - Page " + pageNo + " out of " + NUM_OF_PAGES + "</b>\n\n";
        instruction += "<u>What is a BestBuds Group?</u>\n";
        instruction += "BestBuds Group contains all your friends in your friend group. \uD83C\uDFE0\n " +
                "In this group, you can view all your friends birthdays and other non-sensitive information. " +
                "You will be notified when a friend's birthday is around the corner.\n\n";
        instruction += "<u>What does a BestBuds Group have?</u>\n";
        instruction += "  - Group Name\n";
        instruction += "  - Group Code\n";
        instruction += "  - BestBuds Users\n\n\n";
        instruction += "<em>Each group has its own unique code. Share this code to your friend group.</em>";


        return instruction;
    }
}

