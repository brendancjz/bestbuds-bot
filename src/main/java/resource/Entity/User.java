package resource.Entity;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class User {
    public String name;
    public String code;
    public String desc;
    public Date dob;

    public User() {
        this.name = "null";
        this.code = "null";
        this.desc = "null";
        this.dob = null;
    }

    public User(String name, String code, String desc, Date dob) {
        this.name = name;
        this.code = code;
        this.desc = desc;
        this.dob = dob;
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

    public static Boolean isNull(User user) {
        return user.code.equals("null");
    }
}
