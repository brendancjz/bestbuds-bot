package Command;

import PSQL.PSQL;
import TelegramBot.BestBudsBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import resource.KeyboardMarkup;

import java.net.URISyntaxException;
import java.sql.SQLException;

public class StartCommand extends Command {
    public StartCommand(BestBudsBot bot, Update update, PSQL psql) throws URISyntaxException, SQLException {
        super(bot, update, psql);

    }

    @Override
    public void runCommand() {
        try {
            System.out.println("StartCommand.runCommand()");

            SendMessage message = new SendMessage();
            message.setChatId(super.getChatId().toString());
            message.enableHtml(true);
            message.setReplyMarkup(KeyboardMarkup.continueKB());

            String startMsg = generateIntro(super.getFirstName());

            if (super.getPSQL().isUserRegistered(super.getChatId())) {
                startMsg += " <em>It looks like you are already registered in the database!</em>";
            }

            message.setText(startMsg);
            super.getBot().execute(message);

        } catch (SQLException | TelegramApiException throwables) {
            throwables.printStackTrace();
        }


    }

    @Override
    public void runCallback() {
        try {
            System.out.println("StartCommand.runCallback()");
            Integer messageId = super.getUpdate().getCallbackQuery().getMessage().getMessageId();
            String firstName = super.getUpdate().getCallbackQuery().getMessage().getChat().getFirstName();
            String callData = super.getUpdate().getCallbackQuery().getData();
            Integer chatId = Integer.parseInt(super.getUpdate().getCallbackQuery().getMessage().getChatId().toString());

            EditMessageText newMessage = new EditMessageText();
            newMessage.setChatId(chatId.toString());
            newMessage.setMessageId(messageId);
            newMessage.enableHtml(true);

            Integer currentPageNumber = Integer.parseInt(callData.split("_")[2]);
            System.out.println("Page " + currentPageNumber);

            if (currentPageNumber == 1) {
                newMessage.setReplyMarkup(KeyboardMarkup.continueKB());

                newMessage.setText(generateIntro(firstName));
            } else if (currentPageNumber == 2) {
                newMessage.setReplyMarkup(KeyboardMarkup.navigationKB(currentPageNumber));

                newMessage.setText("Get Started -- Page 2");
            } else if (currentPageNumber == 3) {
                newMessage.setReplyMarkup(KeyboardMarkup.backKB(currentPageNumber));
                newMessage.setText("Get Started -- Page 3");
            }

            super.getBot().execute(newMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }


    }

    private String generateIntro(String name) {
        String intro = "<b>Start</b>\n";

        intro += this.generateBBB();

        intro += "Hi " + name +
                "! Welcome to Best Buds Bot.\n\n";
        intro += "<u>What can this bot do?</u>\n";
        intro += this.generateBotDescription();
        intro += "At any point you need assistance, enter /help! \uD83C\uDD98\n\n";
        intro += "<u>Who created this bot?</u>\n";
        intro += "<a href='brendanchia.com'>Brendan</a>";
        intro += "Curious how the bot stores your data? Well, it uses PostgreSQL! It is an open-source database system with solid capabilities. PSQL is reliable and " +
                "your data is stored in PSQL's encrypted databases, so nothing to worry about!\n\n";
        intro += "<em>You have established a connection with the server. This connection is 24/7.</em>";

        return intro;
    }
}
