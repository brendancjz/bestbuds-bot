package resource.Entity;

import java.sql.Date;

public class Message {
    public String message;
    public User userTo;
    public User userFrom;
    public Boolean hasSent;
    public Date createdOn;

    public Message() {
        message = "";
        userTo = null;
        userFrom = null;
        hasSent = false;
        createdOn = null;
    }

}
