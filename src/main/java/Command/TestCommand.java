package Command;

import PSQL.PSQL;
import TelegramBot.BestBudsBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import resource.KeyboardMarkup;

import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.Arrays;

public class TestCommand extends Command {
    private static final String COMMAND = "test";
    public TestCommand(BestBudsBot bot, Update update, PSQL psql) throws URISyntaxException, SQLException {
        super(bot, update, psql);
    }

    @Override
    public void runCommand() {
        try {
            System.out.println("TestCommand.runCommand()");
            String text = super.getUpdate().getMessage().getText().trim();
            int chatId = Integer.parseInt(super.getUpdate().getMessage().getChatId().toString());
            SendMessage message = new SendMessage();
            message.setChatId(super.getChatId().toString());
            message.enableHtml(true);
            message.setText("Testing...");

            if (text.equals("/" + COMMAND)) { //Bad command. Missing arguments
                missingArgumentsMessage(message);
                return;
            }

            runTestSendCommand(message, text);
        } catch (TelegramApiException | SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private void runTestSendCommand(SendMessage message, String text) throws TelegramApiException, SQLException {

        if (!validateMessage(text)) {
            message.setText("Sorry, invalid code or formatting. Please try again.");
            super.getBot().execute(message);
            return;
        }

        String[] arr = text.split(" ");
        String receiverCode = arr[1];
        String senderMessage = String.join(" ", Arrays.copyOfRange(arr, 2, arr.length));

        if (super.getPSQL().addTestMessage(receiverCode, super.getChatId(), senderMessage)) {
            message.setText("Message sent! I'm sure your BestBud will appreciate this message: \n\n" + senderMessage);
        } else {
            message.setText("Something went wrong in sending. Perhaps BestBud's birthday is not coming yet or y'all do not share the same BestBuds Group.");
        }

        super.getBot().execute(message);
    }
}
