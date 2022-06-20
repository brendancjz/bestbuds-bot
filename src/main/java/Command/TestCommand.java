package Command;

import PSQL.PSQL;
import TelegramBot.BestBudsBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import resource.Entity.Group;
import resource.Entity.Message;
import resource.Entity.User;
import resource.KeyboardMarkup;

import java.net.URISyntaxException;
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
            int chatId = Integer.parseInt(super.getUpdate().getMessage().getChatId().toString());
            SendMessage message = new SendMessage();
            message.setChatId(super.getChatId().toString());
            message.enableHtml(true);
            message.setText("Testing...");

            if (text.equals("/" + COMMAND)) { //Bad command. Missing arguments
                missingArgumentsMessage(message);
                return;
            }

            //runTestSendCommand(message, text);
            User birthdayUser = super.getPSQL().getUserDataResultSet(107270014);

            message.setText(runTest());
            super.getBot().execute(message);
            //runSendMessageToAdminsEvent(birthdayUser, super.getPSQL());
        } catch (TelegramApiException | SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private String runTest() throws SQLException {
        User birthdayUser = super.getPSQL().getUserDataResultSet(107270014);
        Date dateNow = Date.valueOf(LocalDate.now());
        Date birthday = Date.valueOf(LocalDate.of(dateNow.toLocalDate().getYear(), birthdayUser.dob.toLocalDate().getMonthValue(), birthdayUser.dob.toLocalDate().getDayOfMonth()));

        String msg = "";
        //msg += "Timestamp Now: " + Timestamp.valueOf(LocalDateTime.now().plusHours(8)).toString() + "\n";
        msg += "Date Now: " + Date.valueOf(String.valueOf(LocalDateTime.now().plusHours(8))).toString() + "\n";
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
