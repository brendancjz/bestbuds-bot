package Timer;

import PSQL.PSQL;
import TelegramBot.BestBudsBot;

import java.net.URISyntaxException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;

public class HappyBirthdayTimer extends BestBudsTimer {

    public HappyBirthdayTimer(BestBudsBot bestBudsBot) throws URISyntaxException, SQLException {
        super(bestBudsBot);
    }


    public void start() throws URISyntaxException, SQLException {
        System.out.println("Timer.HappyBirthdayTimer has started...");
        PSQL psql = new PSQL();
        ArrayList<String> chatIds = psql.getAllChatId();

        for (String id : chatIds) {
            int chatId = Integer.parseInt(id);
            SendHappyBirthdayMessageTask task = new SendHappyBirthdayMessageTask(super.getBot(), chatId);

            //Get DOB
            //String dob = psql.getUserDOB(chatId);
            //System.out.println("DOB for Id: " + chatId + " is " + dob);

            //timer.schedule(task, scheduleOnBirthdate(dob));
        }

        psql.closeConnection();
    }

    public void startForUser(int chatId) throws URISyntaxException, SQLException {
        System.out.println("Timer.HappyBirthdayTimer has started...");

        PSQL psql = new PSQL();

        SendHappyBirthdayMessageTask task = new SendHappyBirthdayMessageTask(super.getBot(), chatId);

        //Get DOB
        //String dob = psql.getUserDOB(chatId);
        //System.out.println("DOB for Id: " + chatId + " is " + dob);

        //scheduleOnBirthdate(dob);
        //timer.schedule(task, scheduleOnBirthdate(dob));
        psql.closeConnection();
    }

    private Date scheduleOnBirthdate(String dob) {
        String[] birthdateArr = dob.split("-");

        int day = Integer.parseInt(birthdateArr[0]);
        int month = Integer.parseInt(birthdateArr[1]);

        LocalDateTime dateNow = LocalDateTime.now();
        int yearNow = dateNow.getYear();
        int monthNow = dateNow.getMonthValue();
        int dayOfMonthNow = dateNow.getDayOfMonth();
        int hourNow = dateNow.getHour();
        int minNow = dateNow.getMinute();
        int secNow = dateNow.getSecond();
        //System.out.println("dayOfMonthNow: " + dayOfMonthNow + " hourNow: " + ((hourNow + 8) % 24) + " minNow: " + minNow + " secNow: " + secNow);

        LocalDateTime closestBirthdate;

        if (isBeforeBirthdate(day, month, dayOfMonthNow, monthNow)) { //Before timing
            closestBirthdate = LocalDateTime.of(yearNow, month, day, 0, 0, 0);
            System.out.println("It has not passed the timing. Setting timer for later: " + closestBirthdate.toString());
        } else {
            closestBirthdate = LocalDateTime.of(yearNow, month, day, 0, 0, 0).plusYears(1);
            System.out.println("It passed the timing. Setting timer for later: " + closestBirthdate.toString());
        }

        return Date.from(closestBirthdate.atZone(ZoneId.systemDefault()).toInstant());
    }

    private boolean isBeforeBirthdate(int day, int month, int dayOfMonthNow, int monthNow) {
        return (monthNow < month) || (monthNow == month && dayOfMonthNow < day);
    }


}
