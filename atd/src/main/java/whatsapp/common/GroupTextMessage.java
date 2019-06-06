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
        LocalDateTime now = LocalDateTime.now();
        String time = String.format("%d:%d", now.getHour(), now.getMinute());
        return String.format("[%s][%s][%s]%s", time, this.groupName, this.username, this.message);
    }
}
