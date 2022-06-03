package Timer;

import PSQL.PSQL;
import TelegramBot.BestBudsBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import resource.Entity.BirthdayManagement;
import resource.Entity.Group;
import resource.Entity.User;

import java.net.URISyntaxException;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.*;

public class BirthdayCheckerTimer extends BestBudsTimer {
    private static final int NUM_OF_THREADS = 10;
    private static final int CHOSEN_HOUR = 12;
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

        scheduler.scheduleAtFixedRate(checkBirthDateHasBeenUpdated(), setDelayTillNextChosenHour(), ONE_DAY, TimeUnit.SECONDS);
        System.out.println("Delay is in " + (setDelayTillNextChosenHour() / 60) + " minutes");
        //Schedule a daily check if anyone's birthday is 1 week from current date. Add them into a new table.
//        scheduler.scheduleAtFixedRate(checkIncomingBirthdays(), setDelayTillNextChosenHour(), ONE_DAY, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(checkIncomingBirthdays(), 0, ONE_MINUTE, TimeUnit.SECONDS);
        //Schedule a daily check for people to send a msg to the person's incoming birthday. Need a new db table for this. Send msg to everyone else to collate msges. Or remind them

        //Schedule a daily check if anyone's birthday is today. If so, collate all the msges and send.
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
                    System.out.println(birthday.toString());
                    //TESTING
//                    this.runReminderMessageEvent(user.chatId, psql);

                    //Within 7 Days
                    if (birthday.after(dateNow) &&
                            (birthday.before(dateOneWeekFromNow) || birthday.equals(dateOneWeekFromNow))) {
                        psql.addUserIntoBirthdayManagement(user.chatId, birthday);
                        this.runReminderMessageEvent(user.chatId, psql);
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
//                        message.setChatId(user.chatId.toString());
                        message.setChatId("107270014");
                        message.enableHtml(true);
                        message.setText("Hi, your birthday is today!");
                        //super.getBot().execute(message);
                        continue;
                    }
                }

                psql.closeConnection();
            } catch (SQLException | URISyntaxException | TelegramApiException e) {
                e.printStackTrace();
            }
        };
    }

    private void runReminderMessageEvent(Integer chatId, PSQL psql) throws SQLException, TelegramApiException {
        //find the groups this person is in.
        User user = psql.getUserDataResultSet(chatId);
        System.out.println("No. of groups for " + user.name + " is " + user.groups.size());

        //Get has_sent_initial_msg
        BirthdayManagement bdayMgmt = psql.getBirthdayManagementDataResultSet(chatId);
        System.out.println("Has sent initial msg for " + bdayMgmt.user.name + "? " + bdayMgmt.hasSentInitialMessage);

        //Get everyone from these groups except for the user himself
        for (Group group : user.groups) {
            List<User> users = psql.getUsersFromGroupExceptUser(group.code, chatId);

            for (User otherUser : users) {
                //send a msg to these ppl to send a msg to the user chatId
                SendMessage message = new SendMessage();
//            message.setChatId(user.chatId.toString());
                message.setChatId("107270014");
                message.enableHtml(true);

                message.setText("Hello " + otherUser.name + " testing, this msg will appear when someone bday is a week from now and you are reminded to send a msg to them!");
                super.getBot().execute(message);
            }

        }


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
                        message.setChatId("107270014");
                        message.enableHtml(true);
                        message.setText("Hi, you have not set your date of birth. To do so, enter:<pre>  /update &lt;date_of_birth&gt;</pre>");

                        super.getBot().execute(message);
                    }
                }

                psql.closeConnection();
            } catch (TelegramApiException | SQLException | URISyntaxException e) {
                e.printStackTrace();
            }
        };
    }

    private Long setDelayTillNextChosenHour() {
        LocalDateTime dateNow = LocalDateTime.now();
        int yearNow = dateNow.getYear();
        int monthNow = dateNow.getMonthValue();
        int dayOfMonthNow = dateNow.getDayOfMonth();
        int hourNow = dateNow.getHour();
        int minNow = dateNow.getMinute();
        int secNow = dateNow.getSecond();

        if (isBeforeChosenHour(hourNow)) { //Before timing
            long numOfHoursUntil12PM = (CHOSEN_HOUR - 1) - ((hourNow + 8) % 24);
            long numOfMinutesUntil12PM = 60 - minNow;

            return ONE_MINUTE * numOfMinutesUntil12PM + ONE_HOUR * numOfHoursUntil12PM;
        } else {
            long numOfHoursFrom12PM = ((hourNow + 8) % 24) - CHOSEN_HOUR;

            return ONE_DAY - (ONE_MINUTE * (long) minNow + ONE_HOUR * numOfHoursFrom12PM);
        }
    }

    private boolean isBeforeChosenHour(int hourNow) {
        return ((hourNow + 8) % 24) < CHOSEN_HOUR;
    }
}
