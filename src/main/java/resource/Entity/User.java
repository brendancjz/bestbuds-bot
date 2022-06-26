package resource.Entity;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class User {
    public Integer chatId;
    public String name;
    public String code;
    public String desc;
    public Date dob;
    public List<Group> groups;

    public User() {
        this.chatId = null;
        this.name = "null";
        this.code = "null";
        this.desc = "null";
        this.dob = null;
        this.groups = new ArrayList<>();
    }

    public User(String name, String code, String desc, Date dob) {
        this.chatId = null;
        this.name = name;
        this.code = code;
        this.desc = desc;
        this.dob = dob;
        this.groups = new ArrayList<>();
    }

    public String getName() {
        return this.name;
    }

    public String getCode() {
        return this.code;
    }

    public String getDesc() {
        return this.desc;
    }

    public String getDob() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        if (this.dob != null) return dateFormat.format(this.dob);

        return "null";
    }

    public String getBirthday() {
        if (this.dob != null) return this.dob.toLocalDate().getDayOfMonth() + " " + this.dob.toLocalDate().getMonth().toString();
        return "null";
    }

    public static Boolean isNull(User user) {
        return user.code.equals("null");
    }
}
