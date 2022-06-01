package Command.UserCommand;

import Command.Command;
import PSQL.PSQL;
import TelegramBot.BestBudsBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import resource.Entity.User;

import java.net.URISyntaxException;
import java.sql.SQLException;

public class ViewBestBudCommand extends Command {

    public ViewBestBudCommand(BestBudsBot bot, Update update, PSQL psql) throws URISyntaxException, SQLException {
        super(bot, update, psql);
    }

    @Override
    public void runCommand() {
        try {
            System.out.println("ViewBestBudsCommand.runCommand()");
            String text = super.getUpdate().getMessage().getText().trim();

            SendMessage message = new SendMessage();
            message.setChatId(super.getChatId().toString());
            message.enableHtml(true);

            if (text.equals("/view_user")) { //Bad command
                missingArgumentsMessage(message);
                return;
            }

            if (validateText(text)) {
                String userCode = text.split(" ")[1];
                message.setText(generateProfileDetails(userCode));
            }

            super.getBot().execute(message);
        } catch (TelegramApiException | SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private Boolean validateText(String text) {
        String[] arr = text.split(" ");

        return arr.length == 2;
    }

    private String generateProfileDetails(String userCode) throws SQLException {
        String deeds = "";
        User user = super.getPSQL().getUserDataResultSet(userCode);

        deeds += "<b><u>Your BestBud Details:</u></b>\n\n";
        deeds += "<em>Name:</em> " + user.name + "\n";
        deeds += "<em>Code:</em>  " + user.code + "\n";
        deeds += "<em>D.O.B:</em> " + user.getDob() + "\n";
        deeds += "<em>Description:</em> " + user.desc + "\n";


        return deeds;
    }
}
