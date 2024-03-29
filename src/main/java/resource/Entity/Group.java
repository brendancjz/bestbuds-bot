package resource.Entity;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class Group {
    public Long id;
    public String name;
    public String code;
    public String createdBy;
    public Date createdOn;
    public String description;
    public List<User> users;
    public Boolean usersReceiveMessages;

    public Group() {
        this.id = null;
        this.name = "null";
        this.code = "null";
        this.createdBy = "null";
        this.createdOn = null;
        this.description = "null";
        this.users = new ArrayList<>();
        this.usersReceiveMessages = null;
    }

    public String getName() {
        return this.name;
    }

    public String getCode() {
        return this.code;
    }

    public String getCreatedBy() {
        return this.createdBy;
    }

    public String getCreatedOn() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        if (this.createdOn != null) return dateFormat.format(this.createdOn);

        return "null";
    }
    public static Boolean isNull(Group group) {
        return group.code.equals("null");
    }
}
