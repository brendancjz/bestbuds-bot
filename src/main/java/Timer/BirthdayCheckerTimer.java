package Timer;

import PSQL.PSQL;
import TelegramBot.BestBudsBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import resource.Entity.BirthdayManagement;
import resource.Entity.Group;
import resource.Entity.Message;
import resource.Entity.User;

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
//        scheduler.scheduleAtFixedRate(checkIncomingBirthdays(), 0, ONE_MINUTE, TimeUnit.SECONDS);
        //Schedule a daily check for people to send a msg to the person's incoming birthday. Need a new db table for this. Send msg to everyone else to collate msges. Or remind them

        //Schedule a daily check if anyone's birthday is today. If so, collate all the msges and send.
    }

    private Runnable checkBirthdayToday() {
        return () -> {
            System.out.println("Checking User Birthdays 12am.");
            try {
                PSQL psql = new PSQL();
                List<User> users = psql.getAllUsers();

                //Check if birthday is coming up
                Date dateNow = Date.valueOf(LocalDate.now());

                for (User user : users) {
                    if (user.getDob().equals("null")) continue;
                    //User birthday
                    Date birthday = Date.valueOf(LocalDate.of(dateNow.toLocalDate().getYear(), user.dob.toLocalDate().getMonthValue(), user.dob.toLocalDate().getDayOfMonth()));
                    //Today is birthday
                    if (birthday.equals(dateNow)) {
                        SendMessage message = new SendMessage();
                        message.setChatId(user.chatId.toString());
                        message.enableHtml(true);
                        message.setText("Hi, today's your birthday! Here's what your BestBuds have to say about ya!");
                        super.getBot().execute(message);

                        List<Message> messages = psql.getUserMessages(user.code);
                        for (Message msg : messages) {
                            message.setText(msg.message + "\n\nFrom: " + msg.userFrom.name);
                            super.getBot().execute(message);
                        }
                    }
                }

                psql.closeConnection();
            } catch (SQLException | URISyntaxException | TelegramApiException e) {
                e.printStackTrace();
            }
        };
    }

    private Runnable checkIncomingBirthdays() {
        return () -> {
            System.out.println("Checking Incoming User Birthdays.");
            try {
                PSQL psql = new PSQL();
                List<User> users = psql.getAllUsers();

                //Check if birthday is coming up
                Date dateNow = Date.valueOf(LocalDate.now());
                Date dateOneWeekFromNow = Date.valueOf(LocalDate.now().plusDays(7));

                for (User user : users) {
                    if (user.getDob().equals("null")) continue;

                    //User birthday
                    Date birthday = Date.valueOf(LocalDate.of(dateNow.toLocalDate().getYear(), user.dob.toLocalDate().getMonthValue(), user.dob.toLocalDate().getDayOfMonth()));
                    //Within 7 Days
                    if (birthday.after(dateNow) &&
                            (birthday.before(dateOneWeekFromNow) || birthday.equals(dateOneWeekFromNow))) {
                        psql.addUserIntoBirthdayManagement(user.chatId, birthday);
                        this.runReminderMessageEvent(user, psql);
                        continue;
                    }

                    //Birthday has passed
                    if (birthday.before(dateNow)) {
                        psql.removeUserFromBirthdayManagement(user.chatId);
                        continue;
                    }

                    //Today is birthday
                    if (birthday.equals(dateNow)) {
                        SendMessage message = new SendMessage();
                        message.setChatId(user.chatId.toString());
//                        message.setChatId("107270014");
                        message.enableHtml(true);
                        message.setText("Hi, today's your birthday! Here's what your BestBuds have to say about ya!");
                        super.getBot().execute(message);

                        List<Message> messages = psql.getUserMessages(user.code);
                        for (Message msg : messages) {
                            message.setText(msg.message + "\n\nFrom: " + msg.userFrom.name);
                            super.getBot().execute(message);
                        }
                        continue;
                    }
                }

                psql.closeConnection();
            } catch (SQLException | URISyntaxException | TelegramApiException e) {
                e.printStackTrace();
            }
        };
    }

    private void runReminderMessageEvent(User user, PSQL psql) throws SQLException, TelegramApiException {
        //Get has_sent_initial_msg
        BirthdayManagement bdayMgmt = psql.getBirthdayManagementDataResultSet(user.chatId);

        //Get everyone from these groups except for the user himself
        for (Group group : user.groups) {
            List<User> users = psql.getUsersFromGroupExceptUser(group.code, user.chatId);
            String reminderToSendOut = this.generateBirthdayReminder(bdayMgmt, group);
            for (User otherUser : users) {
                //send a msg to these ppl to send a msg to the user chatId
                SendMessage message = new SendMessage();
                message.setChatId(otherUser.chatId.toString());
//                message.setChatId("107270014");
                message.enableHtml(true);
                message.setText(reminderToSendOut);
                super.getBot().execute(message);

                SendMessage message2 = new SendMessage();
                message2.setChatId("107270014");
                message2.enableHtml(true);
                message2.setText(reminderToSendOut);
                super.getBot().execute(message2);
            }
        }

        //Update has_sent_initial to true
        if (!bdayMgmt.hasSentInitialMessage) psql.updateHasSentInitialBirthdayManagement(user.chatId, true);

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
//                        message.setChatId(user.chatId.toString());
                        message.setChatId(user.chatId.toString());
                        message.enableHtml(true);
                        message.setText(this.generateSetBirthdayReminder(user));
                        super.getBot().execute(message);

                        SendMessage message2 = new SendMessage();
                        message2.setChatId("107270014");
                        message2.enableHtml(true);
                        message2.setText(this.generateSetBirthdayReminder(user));
                        super.getBot().execute(message2);
                    }
                }

                psql.closeConnection();
            } catch (TelegramApiException | SQLException | URISyntaxException e) {
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

    private String generateBirthdayReminder(BirthdayManagement bdayMgmt, Group group) {
        String msg = "";
        if (bdayMgmt.hasSentInitialMessage) {
            //Simple reminder
            msg = "Hey, just a reminder that " + bdayMgmt.user.name + " from <em>" + group.name + "</em> is around the corner. please send a birthday message to him/her!";
        } else {
            //Sending it for the first time
            int numOfDaysAway = bdayMgmt.birthday.toLocalDate().compareTo(LocalDate.now());
            System.out.println("Num of Days away: " + numOfDaysAway);

            msg = "Hi, " + bdayMgmt.user.name + " from <em>" + group.name + "</em> coming up on " + bdayMgmt.getBirthday() + "! please send a birthday message to him/her!";
        }

        msg += "\n<pre>  /send " + bdayMgmt.user.code + " &lt;message&gt;</pre>";

        return msg;
    }

    private String generateSetBirthdayReminder(User user) {
        return "Hi " + user.name + ", you have not set your date of birth. To do so, enter:<pre>  /update_dob yyyy-MM-dd</pre>";
    }

    private boolean isBeforeChosenHour(int chosenHour, int hourNow) {
        return ((hourNow + 8) % 24) < chosenHour;
    }
}
