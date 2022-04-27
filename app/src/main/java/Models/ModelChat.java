package Models;

public class ModelChat {

    String message, reciever, sender, timestamp, type;
    boolean isSeen;

    public  ModelChat(){

    }
       public ModelChat (String message, String reciever, String sender, String timestamp, boolean isSeen,String type) {
        this.message = message;
        this.reciever = reciever;
        this.sender = sender;
        this.timestamp = timestamp;
        this.isSeen = isSeen;
        this.type=type;
    }

    public String getMessage () {
        return message;
    }

    public void setMessage (String message) {
        this.message = message;
    }

    public String getReciever () {
        return reciever;
    }

    public void setReciever (String reciever) {
        this.reciever = reciever;
    }

    public String getSender () {
        return sender;
    }

    public void setSender (String sender) {
        this.sender = sender;
    }

    public String getTimestamp () {
        return timestamp;
    }

    public void setTimestamp (String timestamp) {
        this.timestamp = timestamp;
    }

    public String getType () {
        return type;
    }

    public void setType (String type) {
        this.type = type;
    }

    public boolean isSeen () {
        return isSeen;
    }

    public void setSeen (boolean seen) {
        isSeen = seen;
    }
}
