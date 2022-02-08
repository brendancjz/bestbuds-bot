import java.net.URISyntaxException;
import java.sql.SQLException;
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

        System.out.println("DAY: " + day + "    MONTH: " + month);

        return null;
    }
}
