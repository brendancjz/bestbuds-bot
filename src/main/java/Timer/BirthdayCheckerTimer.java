package Timer;

import PSQL.PSQL;
import TelegramBot.BestBudsBot;

import java.net.URISyntaxException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Timer;
import java.util.concurrent.*;

public class BirthdayCheckerTimer extends BestBudsTimer {
    private static final int NUM_OF_THREADS = 10;
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
            System.out.println("Checked User Birthdays has been filled.");
        };

        //scheduler.scheduleAtFixedRate(checkBirthDateHasBeenUpdated, setDelayTillNext12PM(), 1, TimeUnit.DAYS);
        setDelayTillNext12PM();
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
        System.out.println("dayOfMonthNow: " + dayOfMonthNow + " hourNow: " + ((hourNow + 8) % 24) + " minNow: " + minNow + " secNow: " + secNow);

        return 1L;
    }
}
