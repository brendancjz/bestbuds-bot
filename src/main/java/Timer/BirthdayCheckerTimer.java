package Timer;

import PSQL.PSQL;
import TelegramBot.BestBudsBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.net.URISyntaxException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Timer;
import java.util.concurrent.*;

public class BirthdayCheckerTimer extends BestBudsTimer {
    private static final int NUM_OF_THREADS = 10;
    private static final int CHOSEN_HOUR = 12;
    private static final int ONE_MINUTE = 1000 * 60;
    private static final int ONE_HOUR = 1000 * 60 * 60;
    private static final int ONE_DAY = 1000 * 60 * 60 * 24;

    public BirthdayCheckerTimer(BestBudsBot bestBudsBot) throws URISyntaxException, SQLException {
        super(bestBudsBot);
    }

    @Override
    public void start() {
        System.out.println("Timer.BirthdayCheckerTimer has started...");
//        ArrayList<String> chatIds = super.getPSQL().getAllChatId();
//
//        for (String id : chatIds) {
//            int chatId = Integer.parseInt(id);
//            SendHappyBirthdayMessageTask task = new SendHappyBirthdayMessageTask(super.getBot(), chatId);
//
//            //Get DOB
//            //String dob = psql.getUserDOB(chatId);
//            //System.out.println("DOB for Id: " + chatId + " is " + dob);
//
//            //timer.schedule(task, scheduleOnBirthdate(dob));
//        }

        //Schedule a daily check if anyone has not inputted their birthdate.
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(NUM_OF_THREADS);

        Runnable checkBirthDateHasBeenUpdated = () -> {
            System.out.println("Checking that User Birthdays has been filled.");
            SendMessage message = new SendMessage();
            message.setChatId("107270014");
            message.enableHtml(true);
            message.setText("Hello 12pm!");
            try {
                super.getBot().execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        };

        scheduler.scheduleAtFixedRate(checkBirthDateHasBeenUpdated, setDelayTillNext12PM(), 1, TimeUnit.DAYS);
        System.out.println("Delay is " + (setDelayTillNext12PM() / 1000));
        //Schedule a daily check if anyone's birthday is 1 week from current date. Send msg to everyone else to collate msges.

        //Schedule a daily check if anyone's birthday is today. If so, collate all the msges and send.

        super.getPSQL().closeConnection();
    }

    private Long setDelayTillNext12PM() {
        LocalDateTime dateNow = LocalDateTime.now();
        int yearNow = dateNow.getYear();
        int monthNow = dateNow.getMonthValue();
        int dayOfMonthNow = dateNow.getDayOfMonth();
        int hourNow = dateNow.getHour();
        int minNow = dateNow.getMinute();
        int secNow = dateNow.getSecond();

        if (isBefore12PM(hourNow)) { //Before timing
            long numOfHoursUntil12PM = (CHOSEN_HOUR - 1) - ((hourNow + 8) % 24);
            long numOfMinutesUntil12PM = 60 - minNow;

            return ONE_MINUTE * numOfMinutesUntil12PM + ONE_HOUR * numOfHoursUntil12PM;
        } else {
            long numOfHoursFrom12PM = ((hourNow + 8) % 24) - CHOSEN_HOUR;

            return ONE_DAY - (ONE_MINUTE * (long) minNow + ONE_HOUR * numOfHoursFrom12PM);
        }
    }

    private boolean isBefore12PM(int hourNow) {
        return ((hourNow + 8) % 24) < CHOSEN_HOUR;
    }
}
