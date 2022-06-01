package Timer;

import PSQL.PSQL;
import TelegramBot.BestBudsBot;

import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.ArrayList;
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
            try {
                TimeUnit.MILLISECONDS.sleep(300);
                System.out.println("Checked User Birthdays has been filled.");


            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };

        ScheduledFuture<String> resultFuture =
                (ScheduledFuture<String>) scheduler.schedule(checkBirthDateHasBeenUpdated, 1, TimeUnit.SECONDS);

        try {
            System.out.println(resultFuture.get());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        //Schedule a daily check if anyone's birthday is 1 week from current date. Send msg to everyone else to collate msges.

        //Schedule a daily check if anyone's birthday is today. If so, collate all the msges and send.

        super.getPSQL().closeConnection();
    }
}
