package resource.Entity;

import java.sql.Date;

public class Message {
    public String message;
    public Integer id;
    public User userTo;
    public User userFrom;
    public Boolean hasSent;
    public Date createdOn;

    public Message() {
        message = "";
        id = null;
        userTo = null;
        userFrom = null;
        hasSent = false;
        createdOn = null;
    }

    public static Boolean isNull(Message message) {
        return message.id == null;
    }
}
