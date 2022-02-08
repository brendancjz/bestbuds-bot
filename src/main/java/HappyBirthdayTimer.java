import java.net.URISyntaxException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;

public class HappyBirthdayTimer {
    private final PSQL psql;
    private final Timer timer;
    private final BirthdayBot bot;

    public HappyBirthdayTimer(BirthdayBot birthdayBot) throws URISyntaxException, SQLException {
        this.timer = new Timer();
        this.psql = new PSQL();
        this.bot = birthdayBot;
    }

    public void start() throws URISyntaxException, SQLException {
        System.out.println("HappyBirthdayTimer has started...");
        PSQL psql = new PSQL();
        ArrayList<String> chatIds = psql.getAllChatId();

        for (String id : chatIds) {
            int chatId = Integer.parseInt(id);
            SendHappyBirthdayMessageTask task = new SendHappyBirthdayMessageTask(bot, chatId);

            //Get DOB
            String dob = psql.getUserDOB(chatId);
            //System.out.println("DOB for Id: " + chatId + " is " + dob);

            scheduleOnBirthdate(dob);
            //timer.schedule(task, );
        }
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
        System.out.println("dayOfMonthNow: " + dayOfMonthNow + " hourNow: " + ((hourNow + 8) % 24) + " minNow: " + minNow + " secNow: " + secNow);


        return null;
    }
}
