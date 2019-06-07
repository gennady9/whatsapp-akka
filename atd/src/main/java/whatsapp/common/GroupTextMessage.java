package whatsapp.common;

import java.io.Serializable;
import akka.actor.ActorRef;
import java.time.LocalDateTime;


public class GroupTextMessage implements Serializable {
    public final String username;
    public final String groupName;
    public final String message;

    public GroupTextMessage(String username, String groupName, String message) {
        this.username = username;
        this.groupName = groupName;
        this.message = message;
    }

    public String getUsername() {
        return this.username;
    }

    public String getGroupName() {
        return this.groupName;
    }

    public String getMessage() {
        return String.format("[%s][%s][%s]%s", getTime(), this.groupName, this.username, this.message);
    }

    static private String getTime(){
        LocalDateTime now = LocalDateTime.now();
        return (now.getHour()+":"+now.getMinute());
    }
}
