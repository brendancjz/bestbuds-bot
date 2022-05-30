package resource.Entity;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class Group {
    public Long id;
    public String name;
    public String code;
    public String createdBy;
    public Date createdOn;

    public Group() {
        this.id = -1L;
        this.name = "null";
        this.code = "null";
        this.createdBy = "null";
        this.createdOn = null;
    }

    public Group(String name, String code, String createdBy, Date createdOn) {
        this.name = name;
        this.code = code;
        this.createdBy = createdBy;
        this.createdOn = createdOn;
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
