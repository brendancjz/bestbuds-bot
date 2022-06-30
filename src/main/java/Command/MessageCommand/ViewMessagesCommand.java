package Command.MessageCommand;

import Command.Command;
import PSQL.PSQL;
import TelegramBot.BestBudsBot;
import com.google.api.client.util.DateTime;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import resource.Entity.File;
import resource.Entity.Message;
import resource.Entity.User;
import resource.FileResource;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ViewMessagesCommand extends Command {
    public static final String COMMAND = "viewMessages";

    public ViewMessagesCommand(BestBudsBot bot, Update update, PSQL psql) throws URISyntaxException, SQLException {
        super(bot, update, psql);
    }

    @Override
    public void runCommand() {
        try {
            System.out.println("ViewMessagesCommand.runCommand()");
            String text = super.getUpdate().getMessage().getText().trim();

            SendMessage message = new SendMessage();
            message.setChatId(super.getChatId().toString());
            message.enableHtml(true);

            if (text.equals("/view_messages")) { //Bad command
                missingArgumentsMessage(message);
                return;
            }

            if (validateText(text)) {
                String year = text.split(" ")[1];
                User user = super.getPSQL().getUserDataResultSet(super.getChatId());
                List<Message> messages = super.getPSQL().getUserMessagesForYear(user.code, year);
                if (messages.size() > 1) {
                    message.setText("Viewing birthday messages received in " + year);
                    super.getBot().execute(message);

                    SendMessage message1 = new SendMessage();
                    message1.setChatId(super.getChatId().toString());
                    message1.enableHtml(true);
                    for (Message msg : messages) {
                        message1.setText(msg.message + "\n\nFrom: " + msg.userFrom.name);
                        super.getBot().execute(message1);
                        for (File file : msg.files) {
                            FileResource.sendFileToUser(super.getBot(), user.chatId.toString(), file.type, file.path);
                        }
                    }
                } else {
                    message.setText("Sorry, you do not have any birthday messages in " + year);
                    super.getBot().execute(message);
                }
            } else {
                message.setText("Not good");
            }

            super.getBot().execute(message);
        } catch (TelegramApiException | SQLException | URISyntaxException | IOException | InterruptedException throwables) {
            throwables.printStackTrace();
        }
    }

    private boolean validateText(String text) {
        String[] arr = text.split(" ");
        if (arr.length >= 2) {
            String year = arr[1];
            try {
                LocalDate date = LocalDate.of(Integer.parseInt(year), 1, 1);
                return true;
            } catch (DateTimeException e) {
                return false;
            }
        } else {
            return false;
        }
    }
}
