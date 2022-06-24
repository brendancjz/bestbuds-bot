package Command.AnalyticCommand;

import Command.Command;
import PSQL.PSQL;
import TelegramBot.BestBudsBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import resource.KeyboardMarkup;

import java.net.URISyntaxException;
import java.sql.SQLException;

public class UserAnalyticsCommand extends Command {
    private static final Integer NUM_OF_PAGES = 1;
    private static final Integer FIRST_PAGE = 1;
    private static final String COMMAND = "userAnalytics";

    public UserAnalyticsCommand(BestBudsBot bot, Update update, PSQL psql) throws URISyntaxException, SQLException {
        super(bot, update, psql);
    }

    @Override
    public void runCommand() {
        try {
            System.out.println("UserAnalyticsCommand.runCommand()");

            SendMessage message = new SendMessage();
            message.setChatId(super.getChatId().toString());
            message.enableHtml(true);

            if (NUM_OF_PAGES != FIRST_PAGE) message.setReplyMarkup(KeyboardMarkup.continueKB(COMMAND));

            String analyticsMsg = generateAnalytics(FIRST_PAGE);

            message.setText(analyticsMsg);
            super.getBot().execute(message);

        } catch (SQLException | TelegramApiException throwables) {
            System.out.println("Unexpected error occurred.");
            throwables.printStackTrace();
        }
    }

    private String generateAnalytics(Integer pageNo) throws SQLException {
        String msg = "";
        msg += "<b>User Analytics - Page " + pageNo + " out of " + NUM_OF_PAGES + "</b>\n\n";
        msg += "No. of BestBuds: " + super.getPSQL().getAllUsers().size() + "\n\n";
        msg += "No. of BestBuds Groups: " + super.getPSQL().getAllGroups().size() + "\n\n";

        return msg;
    }

    @Override
    public void runCallback() {

    }
}
