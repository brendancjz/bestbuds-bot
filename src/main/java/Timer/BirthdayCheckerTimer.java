package Timer;

import PSQL.PSQL;
import TelegramBot.BestBudsBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import resource.Entity.*;
import resource.FileResource;
import resource.KeyboardMarkup;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.*;

public class BirthdayCheckerTimer extends BestBudsTimer {
    private static final int NUM_OF_THREADS = 10;
    private static final int AFTERNOON = 12;
    private static final int MIDNIGHT = 0;
    private static final int ONE_MINUTE = 60;
    private static final int ONE_HOUR = 60 * 60;
    private static final int ONE_DAY = 60 * 60 * 24;

    public BirthdayCheckerTimer(BestBudsBot bestBudsBot) throws URISyntaxException, SQLException {
        super(bestBudsBot);
    }

    public static void runTurnOffReminderEvent(BestBudsBot bot, Update update, PSQL psql) throws SQLException, URISyntaxException, TelegramApiException {
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        String callData = update.getCallbackQuery().getData();
        String[] arr = callData.split("_");
        String receiverCode = arr[1];
        Integer senderChatId = Integer.parseInt(arr[2]);

        psql.addEmptyMessage(receiverCode, senderChatId);

        EditMessageText newMessage = new EditMessageText();
        newMessage.setChatId(senderChatId.toString());
        newMessage.setMessageId(messageId);
        newMessage.enableHtml(true);
        newMessage.setText("Birthday reminder has been turned off. If you changed your mind, send your message before the birthday with this: \n<pre>  /send " + receiverCode + " &lt;message&gt;</pre>");

        bot.execute(newMessage);
        psql.closeConnection();
    }

    @Override
    public void start() {
        System.out.println("Timer.BirthdayCheckerTimer has started...");

        //Schedule a daily check if anyone has not inputted their birthdate.
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(NUM_OF_THREADS);
        scheduler.scheduleAtFixedRate(checkBirthDateHasBeenUpdated(), setDelayTillNextChosenHour(AFTERNOON), ONE_DAY, TimeUnit.SECONDS);
        //Schedule a daily check if anyone's birthday is 1 week from current date. Add them into a new table.
        scheduler.scheduleAtFixedRate(checkIncomingBirthdays(), setDelayTillNextChosenHour(AFTERNOON), ONE_DAY, TimeUnit.SECONDS);
        //Schedule a daily check if anyone's birthday is today 12am.
        scheduler.scheduleAtFixedRate(checkBirthdayToday(), setDelayTillNextChosenHour(MIDNIGHT), ONE_DAY, TimeUnit.SECONDS);
    }

    private Runnable checkBirthdayToday() {
        return () -> {
            System.out.println("Checking User Birthdays 12am. Today is: " + LocalDate.now().toString());
            try {
                PSQL psql = new PSQL();
                List<User> users = psql.getAllUsers();

                //Check if birthday is coming up
                Date dateNow = Date.valueOf(LocalDateTime.now().plusHours(8).toLocalDate());

                for (User user : users) {
                    if (user.getDob().equals("null")) continue;
                    //User birthday
                    Date birthday = Date.valueOf(LocalDate.of(dateNow.toLocalDate().getYear(), user.dob.toLocalDate().getMonthValue(), user.dob.toLocalDate().getDayOfMonth()));
                    //Today is birthday
                    if (birthday.equals(dateNow)) {
                        runBirthdayEvent(user, psql, birthday);
                    }
                }

                psql.closeConnection();
            } catch (SQLException | URISyntaxException | TelegramApiException | InterruptedException | IOException e) {
                e.printStackTrace();
            }
        };
    }

    public Runnable checkIncomingBirthdays() {
        return () -> {
            System.out.println("Checking Incoming User Birthdays.");
            try {
                PSQL psql = new PSQL();
                List<User> users = psql.getAllUsers();

                //Check if birthday is coming up
                Date dateNow = Date.valueOf(LocalDateTime.now().plusHours(8).toLocalDate());
                Date dateOneWeekFromNow = Date.valueOf(LocalDateTime.now().plusHours(8).toLocalDate().plusDays(7));
                Date dateTwoDaysFromNow = Date.valueOf(LocalDateTime.now().plusHours(8).toLocalDate().plusDays(2));

                for (User user : users) {
                    if (user.getDob().equals("null")) continue;

                    //User birthday
                    Date birthday = Date.valueOf(LocalDate.of(dateNow.toLocalDate().getYear(), user.dob.toLocalDate().getMonthValue(), user.dob.toLocalDate().getDayOfMonth()));
                    //Within 7 Days
                    if (birthday.after(dateNow) &&
                            (birthday.before(dateOneWeekFromNow) || birthday.equals(dateOneWeekFromNow))) {
                        psql.addUserIntoBirthdayManagement(user.chatId, birthday);
                        System.out.println("User " + user.name + " birthday is within the week.");
                        this.runReminderMessageEvent(user, psql);

                        //If birthday is two days from now, send the msges collated to all the admins.
                        if (birthday.equals(dateTwoDaysFromNow)) {
                            System.out.println("User " + user.name + " birthday is two days from now. Sending to admins.");
                            this.runSendMessageToAdminsEvent(user, psql);
                        }
                        continue;
                    }

                    //Birthday has passed
                    if (birthday.before(dateNow)) {
                        System.out.println("User " + user.name + " birthday has passed");
                        psql.removeUserFromBirthdayManagement(user.chatId);
                        continue;
                    }
                }

                psql.closeConnection();
            } catch (SQLException | URISyntaxException | TelegramApiException | IOException | InterruptedException e) {
                e.printStackTrace();
            }
        };
    }

    private void runBirthdayEvent(User user, PSQL psql, Date birthday) throws SQLException, InterruptedException, IOException, URISyntaxException, TelegramApiException {
        System.out.println("User " + user.name + " birthday is today, " + birthday.toString());
        SendMessage message = new SendMessage();
        message.setChatId(user.chatId.toString());
        message.enableHtml(false);

        List<Message> messages = psql.getUserMessages(user.code);
        if (messages.size() > 0) {
            //Send happy birthday sticker
            SendSticker bdaySticker = new SendSticker();
            bdaySticker.setChatId(user.chatId.toString());
            bdaySticker.setSticker(FileResource.getBirthdaySticker());
            super.getBot().execute(bdaySticker);
            message.setText("Hi, today's your birthday! Here's what your BestBuds have to say about ya!");
            super.getBot().execute(message);
        }

        for (Message msg : messages) {
            if (msg.isEmpty == null || !msg.isEmpty) {
                message.setText(msg.message + "\n\nFrom: " + msg.userFrom.name);
                super.getBot().execute(message);
                for (File file : msg.files) {
                    FileResource.sendFileToUser(super.getBot(), user.chatId.toString(), file.type, file.path);
                }
            }

            psql.updateUserMessageToSent(msg.id);
        }

        //Send a message to other bestbuds to inform them that today is who's birthday
        for (Group group : user.groups) {
            List<User> otherUsers = psql.getUsersFromGroupExceptUser(group.code, user.chatId);
            for (User otherUser : otherUsers) {
                SendMessage userBdayTdyMsg = new SendMessage();
                userBdayTdyMsg.setChatId(otherUser.chatId.toString());
                userBdayTdyMsg.enableHtml(true);
                userBdayTdyMsg.setText("Hi, today is " + user.name + " from " + group.name + " birthday!");
                super.getBot().execute(userBdayTdyMsg);
            }
        }
    }

//    public void runBirthdayEventForBirthdayUserOnly(User user, PSQL psql, Date birthday) throws SQLException, InterruptedException, IOException, URISyntaxException, TelegramApiException {
//        System.out.println("BirthdayCheckerTimer.runBirthdayEventForBirthdayUserOnly()");
//        System.out.println("User " + user.name + " birthday is today, " + birthday.toString());
//        SendMessage message = new SendMessage();
//        message.setChatId(user.chatId.toString());
//        message.enableHtml(false);
//
//        List<Message> messages = psql.getUserMessages(user.code);
//
//        doSendBirthdayMessageFromBotToUser(user, messages, message);
//
//        for (Message msg : messages) {
//            message.setText(msg.message + "\n\nFrom: " + msg.userFrom.name);
//            super.getBot().execute(message);
//            for (File file : msg.files) {
//                FileResource.sendFileToUser(super.getBot(), user.chatId.toString(), file.type, file.path);
//            }
//            psql.updateUserMessageToSent(msg.id);
//        }
//    }

//    private void doSendBirthdayMessageFromBotToUser(User user, List<Message> messages, SendMessage message) throws InterruptedException, IOException, URISyntaxException, TelegramApiException {
//        if (messages.size() > 0) {
//            //Send happy birthday sticker
//            SendSticker bdaySticker = new SendSticker();
//            bdaySticker.setChatId(user.chatId.toString());
//            bdaySticker.setSticker(FileResource.getBirthdaySticker());
//            super.getBot().execute(bdaySticker);
//            message.setText("Hi, today's your birthday! Here's what your BestBuds have to say about ya!");
//            super.getBot().execute(message);
//        }
//    }

    public void runSendMessageToAdminsEvent(User user, PSQL psql) throws SQLException, TelegramApiException, IOException, URISyntaxException, InterruptedException {
        //Get everyone from these groups except for the user himself
        for (Group group : user.groups) {
            List<User> admins = psql.getAdminsFromGroup(group.code);

            //send all collated msges so far of this user's birthday to the admins of the groups he is in.
            List<Message> msges = psql.getUserMessagesFromUsersOfGroup(user.code, group.code);
            for (User admin : admins) {
                //Do not send msg to the user if he is the admin too. This is because its premature sending him a happy birthday
                if (admin.code.equals(user.code)) continue;

                runSendMessageToAdminEvent(admin, user, group, msges);

                //Send excel sheet
                //FileResource.generateMessageFile(super.getBot(), admin.chatId.toString(), group.code, user, psql);
            }
        }
    }

    public void runSendMessageToAdminEvent(User admin, User bdayUser, Group group, List<Message> msges) throws TelegramApiException {
        SendMessage message = new SendMessage();
        message.setChatId(admin.chatId.toString());
        message.enableHtml(false);
        message.setText("Hello admin of " + group.name + ", your BestBud " + bdayUser.name + " with user_code " + bdayUser.code + " birthday is coming up. Here are the collated birthday messages from the group.\n\n");
        super.getBot().execute(message);

        for (Message msg : msges) {
            if (msg.isEmpty == null || !msg.isEmpty) {
                message.setText(msg.message + "\n\nFrom: " + msg.userFrom.name + "\nDate: " + msg.createdOn);
                super.getBot().execute(message);
                for (File file : msg.files) {
                    try {
                        FileResource.sendFileToUser(super.getBot(), admin.chatId.toString(), file.type, file.path);
                    } catch (InterruptedException | IOException | URISyntaxException | TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

//    private String collateMessages(List<Message> msges, Group group, User bdayUser) {
//        String message = "";
//        message += "Hello admin of " + group.name + ", your BestBud " + bdayUser.name + " with user_code " + bdayUser.code + " birthday is coming up. Here are the collated birthday messages from the group.\n\n";
//        for (Message msg : msges) {
//            if (msg.isEmpty == null || !msg.isEmpty) {
//                message += msg.message + "\n\nFrom: " + msg.userFrom.name + "\nDate: " + msg.createdOn + "\n\n";
//            }
//        }
//
//        return message;
//    }

    private void runReminderMessageEvent(User user, PSQL psql) throws SQLException {
        //Get has_sent_initial_msg
        BirthdayManagement bdayMgmt = psql.getBirthdayManagementDataResultSet(user.chatId);

        //Get everyone from these groups except for the user himself
        for (Group group : user.groups) {
            List<User> users = psql.getUsersFromGroupExceptUser(group.code, user.chatId);

            for (User otherUser : users) {
                //check if user alr send a msg to the person. if have, no need send reminder
                boolean hasSentBdayMsg = psql.hasUserSentBdayMessageToUser(otherUser.code, user.code, bdayMgmt);
                if (!hasSentBdayMsg) {
                    //send a msg to these ppl to send a msg to the user chatId
                    try {
                        this.runBirthdayReminder(bdayMgmt, group, otherUser);
                    } catch (TelegramApiException e) {
                        //If user blocked the bot, it will throw an error
                        e.printStackTrace();
                        continue;
                    }
                }
            }
        }

        //Update has_sent_initial to true
        if (!bdayMgmt.hasSentInitialMessage) psql.updateHasSentInitialBirthdayManagement(user.chatId, true);
    }

    public void runBirthdayReminder(BirthdayManagement bdayMgmt, Group group, User otherUser) throws TelegramApiException {
        SendMessage message = new SendMessage();
        message.setChatId(otherUser.chatId.toString());
        message.enableHtml(true);

        String msg = "";
        if (bdayMgmt.hasSentInitialMessage) {
            //Simple reminder
            msg = generateBirthdayReminderMessage(bdayMgmt, group);
            message.setReplyMarkup(KeyboardMarkup.toggleBirthdayReminderKB(false, bdayMgmt.user.code, otherUser.chatId));
        } else {
            //Sending it for the first time
            //TODO Replace this with a better msg
            int numOfDaysAway = bdayMgmt.birthday.toLocalDate().compareTo(LocalDate.now());
            System.out.println("Num of Days away: " + numOfDaysAway);
            msg = generateInitialBirthdayMessage(bdayMgmt, group);
        }

        msg += "\n<pre>  /send " + bdayMgmt.user.code + " &lt;message&gt;</pre>";

        message.setText(msg);
        super.getBot().execute(message);
    }

    private Runnable checkBirthDateHasBeenUpdated() {
        return () -> {
            System.out.println("Checking that User Birthdays has been filled.");
            try {
                PSQL psql = new PSQL();
                List<User> users = psql.getAllUsers();

                for (User user : users) {
                    if (user.getDob().equals("null")) {
                        SendMessage message = new SendMessage();
                        message.setChatId(user.chatId.toString());
                        message.enableHtml(true);
                        message.setText(this.generateSetBirthdayReminder(user));
                        try {
                            super.getBot().execute(message);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                            continue;
                        }

                    }
                }

                psql.closeConnection();
            } catch (SQLException | URISyntaxException e) {
                e.printStackTrace();
            }
        };
    }

    private Long setDelayTillNextChosenHour(int chosenHour) {
        LocalDateTime dateNow = LocalDateTime.now();
        int hourNow = dateNow.getHour();
        int minNow = dateNow.getMinute();

        if (isBeforeChosenHour(chosenHour, hourNow)) { //Before timing
            long numOfHoursUntil12PM = (chosenHour - 1) - ((hourNow + 8) % 24);
            long numOfMinutesUntil12PM = 60 - minNow;
            return ONE_MINUTE * numOfMinutesUntil12PM + ONE_HOUR * numOfHoursUntil12PM;
        } else {
            long numOfHoursFrom12PM = ((hourNow + 8) % 24) - chosenHour;
            return ONE_DAY - (ONE_MINUTE * (long) minNow + ONE_HOUR * numOfHoursFrom12PM);
        }
    }

    public String generateInitialBirthdayMessage(BirthdayManagement bdayMgmt, Group group) {
        return "Hi, " + bdayMgmt.user.name + " from <em>" + group.name + "</em> coming up on " + bdayMgmt.getBirthday() + "! please send a birthday message to him/her! " +
                "Replace \"&lt;message&gt;\" with your birthday message to him/her.";
    }

    public String generateBirthdayReminderMessage(BirthdayManagement bdayMgmt, Group group) {
        return "Hey, just a reminder that " + bdayMgmt.user.name + " from <em>" + group.name + "</em> is around the corner. please send a birthday message to him/her! " +
                "Replace \"&lt;message&gt;\" with your birthday message to him/her.";
    }

    private String generateSetBirthdayReminder(User user) {
        return "Hi " + user.name + ", you have not set your date of birth. To do so, enter:<pre>  /update_dob yyyy-MM-dd</pre>";
    }

    private boolean isBeforeChosenHour(int chosenHour, int hourNow) {
        return ((hourNow + 8) % 24) < chosenHour;
    }
}
