package biome.fresco.Objects;

/**
 * Created by Drew on 1/3/2017.
 */

public class MessageObject {
    private String message;
    private String author;
    private long type;
    private long timeStamp;
    private boolean isMe;

    public boolean isMe() {
        return isMe;
    }
    public void setIsMe(boolean isMe){
        this.isMe = isMe;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public long getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public MessageObject(String message, String author, long type, long timeStamp, boolean isMe){
        this.author = author;
        this.timeStamp = timeStamp;
        this.type = type;
        this.message = message;
        this.isMe = isMe;
    }
}
