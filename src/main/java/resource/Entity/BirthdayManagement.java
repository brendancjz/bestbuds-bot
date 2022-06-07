package resource.Entity;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class BirthdayManagement {
    public User user;
    public Date birthday;
    public Boolean hasSentInitialMessage;

    public BirthdayManagement() {
        this.user = new User();
        this.birthday = null;
        this.hasSentInitialMessage = false;
    }

    public String getBirthday() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        if (this.birthday != null) return dateFormat.format(this.birthday);

        return "null";
    }
}
