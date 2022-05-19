package Command;

import PSQL.PSQL;
import TelegramBot.BestBudsBot;
import Timer.HappyBirthdayTimer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.net.URISyntaxException;
import java.sql.SQLException;

public class SubscribeCommand extends Command {
    public SubscribeCommand(BestBudsBot bot, Update update, PSQL psql) throws URISyntaxException, SQLException {
        super(bot, update, psql);

    }

    @Override
    public void runCommand() {
        try {
            System.out.println("SubscribeCommand.runCommand()");
            String text = super.getUpdate().getMessage().getText();
            int chatId = Integer.parseInt(super.getUpdate().getMessage().getChatId().toString());

            SendMessage message = new SendMessage();
            message.setChatId(super.getChatId().toString());
            message.enableHtml(true);

            if (text.equals("/subscribe")) { //Instructions to create or join a Group

                message.setText(generateSubscribeInstruction());
                super.getBot().execute(message);

            } else {
                invalidMessage(message, text);
            }

//            String[] arr = text.split(" ");
//
//            if (arr.length == 3) {
//                String firstName = arr[1];
//                String date = arr[2];
//
//                if (validateDate(date) && !super.getPSQL().isUserRegistered(chatId)) {
//                    super.getPSQL().addNewUser(chatId, firstName, date);
//                    message.setText("Thanks! Your name is " + firstName + " and your D.O.B is " + date + ".");
//                    super.getBot().execute(message);
//
//                    scheduleBirthdayMessage(chatId);
//                } else if (super.getPSQL().isUserRegistered(chatId)) {
//                    message.setText("You are already registered.");
//
//                    super.getBot().execute(message);
//                } else {
//                    wrongDateFormatMessage(message);
//                }
//            } else {
//                missingArgumentsMessage(message);
//            }
        } catch (TelegramApiException throwables) {
            throwables.printStackTrace();
        }


    }

    private String generateSubscribeInstruction() {
        String instruction = "";

        instruction += "<b>Subscribe</b> \n\n";
        instruction += "<u>What is a BestBuds Group?</u>\n";
        instruction += "BestBuds Group contains all your friends in your friend group. \uD83C\uDFE0\n " +
                "In this group, you can view all your friends birthdays and other non-sensitive information. " +
                "You will be notified when a friend's birthday is around the corner.\n\n";
        instruction += "Subscribe to a BestBuds group by creating or joining one. Tap on the command below to copy text.\n\n\n";
        instruction += "<b>BestBuds Group Commands</b> \n\n";
        instruction += "<em>Join a BestBuds Group</em>\n";
        instruction += "<pre>  /join &lt;group_code&gt;</pre>\n\n";
        instruction += "<em>Create a BestBuds Group</em>\n";
        instruction += "<pre>  /create &lt;group_name&gt;</pre>\n\n";
        instruction += "<em>Leave a BestBuds Group</em>\n";
        instruction += "<pre>  /exit &lt;group_code&gt;</pre>\n\n";
        instruction += "<em>View all BestBuds in BestBuds Group</em>\n";
        instruction += "<pre>  /view_bestbuds &lt;group_code&gt;</pre>\n\n";
        instruction += "<em>View BestBuds Group Details</em>\n";
        instruction += "<pre>  /view_group &lt;group_code&gt;</pre>\n\n\n";
        instruction += "<b>BestBuds Group Commands</b> \n\n";
        instruction += "<em>Remove a BestBud</em>\n";
        instruction += "<pre>  /remove  &lt;user_code&gt; &lt;group_code&gt;</pre>\n\n";

        return instruction;
    }

    private void scheduleBirthdayMessage(int chatId) throws URISyntaxException, SQLException {
        //TODO: The scheduling
        //Send Happy Birthday
        HappyBirthdayTimer timer = new HappyBirthdayTimer(super.getBot());
        timer.startForUser(chatId);
    }
}

