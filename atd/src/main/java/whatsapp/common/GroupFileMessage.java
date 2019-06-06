package whatsapp.common;

import java.io.Serializable;
import akka.actor.ActorRef;

public class GroupFileMessage implements Serializable {
    public final String username;
    public final String groupName;
    public final byte[] file;

    public GroupFileMessage(String username, String groupName, byte[] file) {
        this.username = username;
        this.groupName = groupName;
        this.file = file;
    }

    public String getUsername() {
        return this.username;
    }

    public String getGroupName() {
        return this.groupName;
    }

    public byte[] getFile() {
        return this.file;
    }
}
