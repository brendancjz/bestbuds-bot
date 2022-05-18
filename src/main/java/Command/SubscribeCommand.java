package Command;

import PSQL.PSQL;
import TelegramBot.BestBudsBot;
import Timer.HappyBirthdayTimer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.net.URISyntaxException;
import java.sql.SQLException;
import java.text.ParseException;

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

//            if (text.equals("/subscribe")) { //Bad command
//                missingArgumentsMessage(message);
//                return;
//            }

            if (text.equals("/subscribe")) { //Instructions to create or join a Group

                message.setText(generateSubscribeInstruction());
                super.getBot().execute(message);
                return;
            }

            String[] arr = text.split(" ");

            if (arr.length == 3) {
                String firstName = arr[1];
                String date = arr[2];

                if (validateDate(date) && !super.getPSQL().isUserRegistered(chatId)) {
                    super.getPSQL().addNewUser(chatId, firstName, date);
                    message.setText("Thanks! Your name is " + firstName + " and your D.O.B is " + date + ".");
                    super.getBot().execute(message);

                    scheduleBirthdayMessage(chatId);
                } else if (super.getPSQL().isUserRegistered(chatId)) {
                    message.setText("You are already registered.");

                    super.getBot().execute(message);
                } else {
                    wrongDateFormatMessage(message);
                }
            } else {
                missingArgumentsMessage(message);
            }
        } catch (SQLException | TelegramApiException | ParseException | URISyntaxException throwables) {
            throwables.printStackTrace();
        }


    }

    private String generateSubscribeInstruction() {
        String instruction = "";

        instruction += "<b>Subscribe</b> \n\n";
        instruction += "You can create or join a BestBuds group.\n\n";
        instruction += "<em>Joining a BestBuds Group</em>\n";
        instruction += "Use this command to join: /join &lt;group_code&gt;\n\n";

        return instruction;
    }

    private void scheduleBirthdayMessage(int chatId) throws URISyntaxException, SQLException {
        //TODO: The scheduling
        //Send Happy Birthday
        HappyBirthdayTimer timer = new HappyBirthdayTimer(super.getBot());
        timer.startForUser(chatId);
    }
}

