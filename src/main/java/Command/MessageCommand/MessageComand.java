package Command.MessageCommand;

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

public class MessageComand extends Command {
    private static final Integer NUM_OF_PAGES = 1;
    private static final Integer FIRST_PAGE = 1;
    private static final String COMMAND = "message";

    public MessageComand(BestBudsBot bot, Update update, PSQL psql) throws URISyntaxException, SQLException {
        super(bot, update, psql);
    }

    @Override
    public void runCommand() {
        try {
            System.out.println("MessageCommand.runCommand()");
            String text = super.getUpdate().getMessage().getText();
            int chatId = Integer.parseInt(super.getUpdate().getMessage().getChatId().toString());

            SendMessage message = new SendMessage();
            message.setChatId(super.getChatId().toString());
            message.enableHtml(true);

            if (NUM_OF_PAGES != FIRST_PAGE) message.setReplyMarkup(KeyboardMarkup.continueKB(COMMAND));

            if (text.equals("/" + COMMAND)) { //Instructions to send messages

                message.setText(generateMessageInstruction(FIRST_PAGE));
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
            System.out.println("MessageCommand.runCallback()");
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
                newMessage.setText(generateMessageInstruction(currentPageNumber));
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

    private String generateMessageInstruction(Integer pageNo) {
        String instruction = "";

        instruction += "<b>Message - Page " + pageNo + " out of " + NUM_OF_PAGES + "</b>\n\n";
        instruction += "<u>What is a BestBuds Message?</u>\n";
        instruction += "BestBuds Message is a birthday message specific to a BestBud. BestBuds Bot first reminds " +
                "everyone else in a BestBuds Group to send a birthday message to a BestBud. On the BestBud's " +
                "birthday, the bot collates and sends the list of birthday messages.\n\n\n";
        instruction += "<b>BestBuds Message Commands</b>\n";
        instruction += "<em>If you're on mobile, tap on the command to copy the text.</em>\n\n";
        instruction += "<em>Send message to BestBud</em>\n";
        instruction += "<pre>  /send &lt;user_code&gt; &lt;message&gt;</pre>\n\n\n";
        instruction += "<em>Each user has its own unique code. This code is used BestBud identification during the automation process.</em>";

        return instruction;
    }
}
