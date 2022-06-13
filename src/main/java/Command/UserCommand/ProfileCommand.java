package Command.UserCommand;

import Command.Command;
import PSQL.PSQL;
import TelegramBot.BestBudsBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import resource.Entity.User;
import resource.KeyboardMarkup;

import java.net.URISyntaxException;
import java.sql.SQLException;

public class ProfileCommand extends Command {

    private static final Integer NUM_OF_PAGES = 2;
    private static final Integer FIRST_PAGE = 1;
    private static final String COMMAND = "profile";
    
    public ProfileCommand(BestBudsBot bot, Update update, PSQL psql) throws URISyntaxException, SQLException {
        super(bot, update, psql);
    }

    @Override
    public void runCommand() {
        try {
            System.out.println("ProfileCommand.runCommand()");
            String text = super.getUpdate().getMessage().getText().trim();
            int chatId = Integer.parseInt(super.getUpdate().getMessage().getChatId().toString());

            SendMessage message = new SendMessage();
            message.setChatId(super.getChatId().toString());
            message.enableHtml(true);

            if (NUM_OF_PAGES != FIRST_PAGE) message.setReplyMarkup(KeyboardMarkup.continueKB(COMMAND));

            if (text.equals("/" + COMMAND)) { //Information on User
                SendMessage message2 = new SendMessage();
                message2.setChatId(super.getChatId().toString());
                message2.enableHtml(true);
                message2.setText(generateProfileDetails(chatId));
                super.getBot().execute(message2);

                message.setText(generateProfileInformation(FIRST_PAGE));
                super.getBot().execute(message);

            } else {
                invalidMessage(message, text);
            }

        } catch (TelegramApiException | SQLException throwables) {
            throwables.printStackTrace();
        }


    }

    @Override
    public void runCallback() {
        try {
            System.out.println("SubscribeCommand.runCallback()");
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

            setCorrectKeyboard(newMessage, currentPageNumber);

            if (currentPageNumber == 1) {
                newMessage.setText(generateProfileInformation(currentPageNumber));
            } else if (currentPageNumber == 2) {
                newMessage.setText(generateCommandList(currentPageNumber));
            }

            super.getBot().execute(newMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void setCorrectKeyboard(EditMessageText newMessage, Integer pageNo) {
        if (NUM_OF_PAGES == FIRST_PAGE) {
            return;
        }

        if (pageNo == FIRST_PAGE) {
            newMessage.setReplyMarkup(KeyboardMarkup.continueKB(COMMAND));
        } else if (pageNo == NUM_OF_PAGES) {
            newMessage.setReplyMarkup(KeyboardMarkup.backKB(COMMAND, pageNo));
        } else {
            newMessage.setReplyMarkup(KeyboardMarkup.navigationKB(COMMAND, pageNo));
        }
    }

    private String generateProfileDetails(Integer chatId) throws SQLException {
        String deeds = "";
        User user = super.getPSQL().getUserDataResultSet(chatId);

        if (user != null) {
            deeds += "<b><u>Your BestBud Details:</u></b>\n\n";
            deeds += "<em>Name:</em> " + user.name + "\n";
            deeds += "<em>Code:</em>  " + user.code + "\n";
            deeds += "<em>D.O.B:</em> " + user.getDob() + "\n";
            deeds += "<em>Description:</em> " + user.desc + "\n";

        } else {
            deeds = "Missing profile details.";
        }

        return deeds;
    }

    private String generateCommandList(Integer pageNo) {
        String instruction = "";
        instruction += "<b>BestBuds User Commands - Page " + pageNo + " out of " + NUM_OF_PAGES + "</b>\n";
        instruction += "<em>If you're on mobile, tap on the command to copy the text.</em>\n";
        instruction += "<em>If you're on desktop, triple click on the command to copy the text.</em>\n\n";
        instruction += "<pre>&lt;first_name&gt; -- &lt;no_whitespace&gt;</pre>\n";
        instruction += "<pre>&lt;date_of_birth&gt; -- &lt;yyyy-MM-dd&gt;</pre>\n\n";
        instruction += "<em>Update Name and DOB</em>\n";
        instruction += "<pre>  /update &lt;first_name&gt; &lt;date_of_birth&gt;</pre>\n\n";
        instruction += "<em>Update Self Description</em>\n";
        instruction += "<pre>  /update_desc &lt;description&gt;</pre>\n\n";
        instruction += "<em>Update Name</em>\n";
        instruction += "<pre>  /update_name &lt;first_name&gt;</pre>\n\n";
        instruction += "<em>Update DOB</em>\n";
        instruction += "<pre>  /update_dob &lt;date_of_birth&gt;</pre>\n\n";
        instruction += "<em>View BestBud Details</em>\n";
        instruction += "<pre>  /view_user &lt;user_code&gt;</pre>\n\n\n";

        return instruction;
    }

    private String generateProfileInformation(Integer pageNo) {
        String info = "";

        info += "<b>Profile - Page " + pageNo + " out of " + NUM_OF_PAGES + "</b>\n\n";
        info += "<u>What is a BestBud?</u>\n";
        info += "A BestBud is a user who uses BestBuds Bot. You are a BestBud :).\n\n ";
        info += "<u>What does a BestBud have?</u>\n";
        info += "  - User Name\n";
        info += "  - User Code\n";
        info += "  - User Date of Birth\n";
        info += "  - One Line Self Description\n";
        info += "  - BestBud Group(s)\n\n\n";
        info += "<em>Each user has its own unique code. This code is used BestBud identification during the messaging process.</em>";


        return info;
    }
}
