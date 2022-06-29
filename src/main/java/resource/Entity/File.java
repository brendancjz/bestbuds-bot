package resource.Entity;

import java.sql.Date;

public class File {
    public static final String DOCUMENT = "DOCUMENT";
    public static final String VIDEO = "VIDEO";
    public static final String PHOTO = "PHOTO";
    public static final String STICKER = "STICKER";
    public Integer id;
    public String type;
    public String path;
    public Date createdOn;
}
