package resource.Entity;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class Message {
    public String message;
    public Integer id;
    public User userTo;
    public User userFrom;
    public Boolean hasSent;
    public Date createdOn;
    public Boolean isEmpty;
    public List<File> files;

    public Message() {
        message = "";
        id = null;
        userTo = null;
        userFrom = null;
        hasSent = false;
        createdOn = null;
        isEmpty = null;
        files = new ArrayList<>();
    }

    public static Boolean isNull(Message message) {
        return message.id == null;
    }
}
