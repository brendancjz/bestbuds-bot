package Command;

import PSQL.PSQL;
import TelegramBot.BestBudsBot;
import Timer.BirthdayCheckerTimer;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import resource.Entity.BirthdayManagement;
import resource.Entity.Group;
import resource.Entity.Message;
import resource.Entity.User;
import resource.FileResource;
import resource.KeyboardMarkup;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

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
            SendMessage message = new SendMessage();
            message.setChatId(super.getChatId().toString());
            message.enableHtml(true);
            message.setText("Testing...");

            super.getBot().execute(message);

//            User mom = super.getPSQL().getUserDataResultSet("Bern9074");
//            FileResource.generateMessageFile(super.getBot(), super.getChatId().toString(), "Chia5976", mom, super.getPSQL());

            runBirthdayTest();

        } catch (TelegramApiException | URISyntaxException | SQLException | InterruptedException | IOException throwables) {
            throwables.printStackTrace();
        }
    }

    private void runBirthdayTest() throws URISyntaxException, SQLException, InterruptedException, TelegramApiException, IOException {
        BirthdayCheckerTimer timer = new BirthdayCheckerTimer(super.getBot());
        User des = super.getPSQL().getUserDataResultSet("Des1483");
        User bernie = super.getPSQL().getUserDataResultSet("Bern9074");
        Group chiaSeed = super.getPSQL().getGroupDataResultSet("ChiaSeeds");
        BirthdayManagement bdayMgmt = super.getPSQL().getBirthdayManagementDataResultSet(des.chatId);
        timer.runBirthdayReminder(bdayMgmt, chiaSeed, bernie);
    }

    private String sendMsg() {
        return "Hi, today's your birthday! Here's what your BestBuds have to say about ya!";
    }

    private String runTest() throws SQLException {
        User birthdayUser = super.getPSQL().getUserDataResultSet(107270014);
        Date dateNow = Date.valueOf(LocalDate.now());
        Date birthday = Date.valueOf(LocalDate.of(dateNow.toLocalDate().getYear(), birthdayUser.dob.toLocalDate().getMonthValue(), birthdayUser.dob.toLocalDate().getDayOfMonth()));

        String msg = "";
        //msg += "Timestamp Now: " + Timestamp.valueOf(LocalDateTime.now().plusHours(8)).toString() + "\n";
        msg += "Time now: " + LocalDateTime.now().plusHours(8).toString() + "\n";
        msg += "Date Now: " + Date.valueOf(LocalDateTime.now().plusHours(8).toLocalDate()).toString() + "\n";
        msg += "Birthday: " + birthday.toString() + "\n";
        msg += "Bday is today: " + birthday.equals(dateNow);

        return msg;
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

    private void runSendMessageToAdminsEvent(User user, PSQL psql) throws SQLException, TelegramApiException {
        //Get everyone from these groups except for the user himself
        for (Group group : user.groups) {
            List<User> admins = psql.getAdminsFromGroup(group.code);

            //send all collated msges so far of this user's birthday to the admins of the groups he is in.
            List<Message> msges = psql.getUserMessagesFromUsersOfGroup(user.code, group.code);
            for (User admin : admins) {
                //DO not send msg to the user if he is the admin too. This is because its premature sending him a happy birthday
                if (admin.code.equals(user.code)) continue;

                SendMessage message = new SendMessage();
                message.setChatId(admin.chatId.toString());
//                message.setChatId("107270014");
                message.enableHtml(true);
                message.setText(collateMessages(msges));
                super.getBot().execute(message);
            }
        }
    }

    private String collateMessages(List<Message> msges) {
        String message = "";
        for (Message msg : msges) {
            message += msg.message + "\n\nFrom: " + msg.userFrom.name + "\n\n";
        }

        return message;
    }
}
