package Command.MessageCommand;

import Command.Command;
import PSQL.PSQL;
import TelegramBot.BestBudsBot;
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
import java.util.Arrays;

public class SendFileCommand extends Command {
    public SendFileCommand(BestBudsBot bot, Update update, PSQL psql) throws URISyntaxException, SQLException {
        super(bot, update, psql);
    }

    @Override
    public void runCommand() {
        try {

            if (super.getChatId().toString().equals("107270014")) {
                runTestCommand();
                return;
            }

            SendMessage message = new SendMessage();
            message.setChatId(super.getChatId().toString());
            message.enableHtml(true);

            User user = super.getPSQL().getUserDataResultSet(super.getChatId());
            String text = user.latestText;

            //Check that the latest text is /send
            if (!isSendMessageTheLatestTextOfUser(text)) {
                message.setText(generateErrorMsg());
                super.getBot().execute(message);
                return;
            }

            //Save the filePath along with the message
            String[] arr = text.split(" ");
            Message msg = super.getPSQL().getMessageByMessageText(arr[1], String.join(" ", Arrays.copyOfRange(arr, 2, arr.length)));

            if (Message.isNull(msg)) {
                message.setText(generateErrorMsg());
                super.getBot().execute(message);
                return;
            }

            String filePath = FileResource.getFilePathOfUploadedFileByUser(FileResource.getFileIdFromUpdate(super.getUpdate()));

            //NOTE: Code does not allow animated stickers with .tgs format at the moment
            if (filePath.contains(".tgs")) {
                message.setText("Sorry, we do not support animated stickers at the moment. Please send a non-animated sticker instead.");
                super.getBot().execute(message);
                return;
            }

            super.getPSQL().addFile(getFileType(super.getUpdate()),filePath, msg.id);

            message.setText("Received your image/video/sticker! " + msg.userTo.name + " will definitely appreciate this!");
            super.getBot().execute(message);
        } catch (InterruptedException | TelegramApiException | IOException | URISyntaxException | SQLException e) {
            e.printStackTrace();
        }

    }

    private void runTestCommand() throws InterruptedException, IOException, URISyntaxException, TelegramApiException {
        String filePath = FileResource.getFilePathOfUploadedFileByUser(FileResource.getFileIdFromUpdate(super.getUpdate()));
        //NOTE: Code does not allow animated stickers with .tgs format at the moment
        SendMessage message = new SendMessage();
        message.setChatId(super.getChatId().toString());
        message.enableHtml(true);
        if (filePath.contains(".tgs")) {
            message.setText("Sorry, we do not support animated stickers at the moment. Please send a non-animated sticker instead.");
            super.getBot().execute(message);
            return;
        }
        FileResource.sendFileToUser(super.getBot(),super.getChatId().toString(),"STICKER", filePath);
    }

    private String generateErrorMsg() {
        String msg = "";
        msg += "Sorry, please send your pictures and videos ONLY after sending a birthday message to a BestBud. " +
                "This allows the bot to map your media files to that birthday message.\n\n" +
                "To send media files along with your birthday message, send the text message first with this command:\n" +
                "<pre>  /send &lt;user_code&gt; &lt;message&gt;</pre>\n" +
                "Next, send your media files immediately after.";

        return msg;
    }

    private boolean isSendMessageTheLatestTextOfUser(String text) throws SQLException {
        if (!text.contains("/" + SendCommand.COMMAND)) return false;

        String[] arr = text.split(" ");
        String receiverCode = arr[1];
        String senderMessage = String.join(" ", Arrays.copyOfRange(arr, 2, arr.length));

        return !User.isNull(super.getPSQL().getUserDataResultSet(receiverCode));
    }

    private String getFileType(Update update) {
        String fileType = "";
        if (update.getMessage().hasDocument()) {
            System.out.println("onUpdateReceived.hasDocument()");
            fileType = File.DOCUMENT;
        } else if (update.getMessage().hasPhoto()) {
            System.out.println("onUpdateReceived.hasPhoto()");
            fileType = File.PHOTO;
        } else if (update.getMessage().hasVideo()) {
            System.out.println("onUpdateReceived.hasVideo()");
            fileType = File.VIDEO;
        } else if (update.getMessage().hasSticker()) {
            System.out.println("onUpdateReceived.hasSticker()");
            fileType = File.STICKER;
        }

        return fileType;
    }
}
