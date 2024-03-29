package Command.MessageCommand;

import Command.Command;
import PSQL.PSQL;
import TelegramBot.BestBudsBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.Arrays;

public class SendCommand extends Command {
    public static final String COMMAND = "send";
    public SendCommand(BestBudsBot bot, Update update, PSQL psql) throws URISyntaxException, SQLException {
        super(bot, update, psql);
    }

    @Override
    public void runCommand() {
        try {
            System.out.println("SendCommand.runCommand()");
            String text = super.getUpdate().getMessage().getText().trim();

            SendMessage message = new SendMessage();
            message.setChatId(super.getChatId().toString());
            message.enableHtml(false);

            if (text.equals("/send")) { //Bad command
                missingArgumentsMessage(message);
                return;
            }

            runActualCommand(message, text);
        } catch (TelegramApiException | SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private void runActualCommand(SendMessage message, String text) throws TelegramApiException, SQLException {

        if (!validateMessage(text)) {
            message.setText("Sorry, invalid code or formatting. Please try again.");
            super.getBot().execute(message);
            return;
        }

        String[] arr = text.split(" ");
        String receiverCode = arr[1];
        String senderMessage = String.join(" ", Arrays.copyOfRange(arr, 2, arr.length));

        if (super.getPSQL().addMessage(receiverCode, super.getChatId(), senderMessage)) {
            message.setText(generateMessageSentText(senderMessage));
        } else {
            message.setText("Something went wrong in sending. Perhaps BestBud's birthday is not coming yet or y'all do not share the same BestBuds Group.");
        }

        super.getBot().execute(message);
    }

    public String generateMessageSentText(String senderMessage) {
        String msg = "";
        msg += "Message sent! I'm sure your BestBud will appreciate this message: \n\n" + senderMessage;
//        msg += "\n\n\nDo you have any pictures/videos of your BestBud? Please send them over now! Stickers are welcome too. If not, no additional response is required.";
        msg += "\n\nIf you would like to update your birthday message, simple use the above /send command with your updated message.";
        return msg;
    }
}
