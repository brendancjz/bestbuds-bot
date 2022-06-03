package resource.Entity;

import java.sql.Date;

public class BirthdayManagement {
    public User user;
    public Date birthday;
    public Boolean hasSentInitialMessage;

    public BirthdayManagement() {
        this.user = new User();
        this.birthday = null;
        this.hasSentInitialMessage = false;
    }
}
