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
import java.text.ParseException;

public class StartCommand extends Command {
    private static final Integer NUM_OF_PAGES = 3;
    private static final Integer FIRST_PAGE = 1;
    private static final String COMMAND = "start";

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

            if (NUM_OF_PAGES != FIRST_PAGE) message.setReplyMarkup(KeyboardMarkup.continueKB(COMMAND));

            String startMsg = generateIntro(super.getFirstName(), FIRST_PAGE);
            startMsg += getRegisteredMsg();

            message.setText(startMsg);
            super.getBot().execute(message);
        } catch (SQLException | TelegramApiException throwables) {
            System.out.println("Unexpected error occurred.");
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

            setCorrectKeyboard(newMessage, currentPageNumber);

            if (currentPageNumber == 1) {
                newMessage.setText(generateIntro(firstName, currentPageNumber) + getRegisteredMsg());
            } else if (currentPageNumber == 2) {
                newMessage.setText(generateGetStarted(currentPageNumber) + getRegisteredMsg());
            } else if (currentPageNumber == 3) {
                newMessage.setText(generateDemo(currentPageNumber) + getRegisteredMsg());
            }

            super.getBot().execute(newMessage);
        } catch (TelegramApiException | SQLException e) {
            e.printStackTrace();
        }
    }

    private String getRegisteredMsg() throws SQLException {
        if (super.getPSQL().isUserRegistered(super.getChatId())) {
            return " <em>It looks like you are already registered in the database!</em>";
        } else {
            super.getPSQL().addNewUser(super.getChatId(), super.getFirstName());
            return " <em>You have been registered!</em>";
        }
    }

    private void setCorrectKeyboard(EditMessageText newMessage, Integer pageNo) {
        if (pageNo == FIRST_PAGE) {
            newMessage.setReplyMarkup(KeyboardMarkup.continueKB(COMMAND));
        } else if (pageNo == NUM_OF_PAGES) {
            newMessage.setReplyMarkup(KeyboardMarkup.backKB(COMMAND, pageNo));
        } else {
            newMessage.setReplyMarkup(KeyboardMarkup.navigationKB(COMMAND, pageNo));
        }
    }

    private String generateIntro(String name, Integer pageNo) {
        System.out.println("StartCommand.generateIntro()");
        String intro = "<b>Start - Page " + pageNo + " out of " + NUM_OF_PAGES + "</b>\n";

        intro += this.generateBBB();

        intro += "Hi " + name +
                "! Welcome to Best Buds Bot.\n\n";
        intro += "<u>What can this bot do?</u>\n";
        intro += this.generateBotDescription();
        intro += "<u>Why should I use this bot?</u>\n";
        intro += "It's difficult to discreetly collate a friend's birthday messages within a friend group. " +
                "BestBuds Bot helps to automate this process.\n\n";
        intro += "<u>How do I use this bot?</u>\n";
        intro += "There are useful commands available to navigate and use BestBuds Bot. Continue to the next page to find out more.\n\n";
        intro += "<u>A friend asked me to join a BestBuds group, what should I do?</u>\n";
        intro += "You should have the command to join the group. It looks something like /join Grp1234. Simply copy and paste that command below and send the message.\n\n";
        intro += "<u>Who created this bot?</u>\n";
        intro += "<a href='brendanchia.com'>Brendan</a>\n\n\n";
        intro += "<em>You have established a connection with the server. This connection is 24/7.</em>";

        return intro;
    }
    
    private String generateGetStarted(Integer pageNo) {
        String info = "<b>Get Started - Page " + pageNo + " out of " + NUM_OF_PAGES + "</b>\n";

        info += this.generateBBB();
        info += "<b>[STEP 1] Update your Personal Details</b>\n";
        info += "  Enter /profile for user commands.\n\n";
        info += "<b>[STEP 2] Create or Join a BestBuds Group</b>\n";
        info += "  Enter /subscribe for group commands.\n\n";
        info += "<b>[STEP 3] Send and Receive Birthday Messages</b>\n";
        info += "  Enter /message for messaging commands\n\n\n";
        info += "Curious how the bot stores your data? Well, it uses PostgreSQL! It is an open-source database system with solid capabilities. PSQL is reliable and " +
                "your data is stored in PSQL's encrypted databases, so nothing to worry about!\n\n";
        info += "At any point you need assistance, enter /help! \uD83C\uDD98\n\n\n\n";
        info += "<em>You have established a connection with the server. This connection is 24/7.</em>";

        return info;
    }

    private String generateDemo(Integer pageNo) {
        String demo = "";

        demo += "<b>Demostration - Page " + pageNo + " out of " + NUM_OF_PAGES + "</b>\n";
        demo += generateBBB();
        demo += "<b>New User</b>\n";
        demo += "<pre>  /update_name Brendan Chia</pre>\n";
        demo += "<pre>  /update_dob 1999-01-01</pre>\n";
        demo += "<pre>  /update_desc Hit me up with tech project ideas! \uD83D\uDE04</pre>\n\n";

        demo += "<b>Join a BestBuds Group</b>\n";
        demo += "<pre>  /join SAMPLE_GRP_CODE</pre>\n\n";

        demo += "<b>Create a BestBuds Group</b>\n";
        demo += "<pre>  /create SAMPLE_GRP_NAME</pre>\n\n";

        demo += "<b>Send message to BestBud</b>\n";
        demo += "<pre>  /send USER_CODE Happy Birthday!!</pre>\n\n\n";

        demo += "<em>You have established a connection with the server. This connection is 24/7.</em>";

        return demo;
    }
}
