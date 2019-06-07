package whatsapp.common;

import java.io.Serializable;

public class LeaveGroupMessage implements Serializable {
    final String groupName;
    final String username;

    public LeaveGroupMessage(String groupName, String username) {
        this.groupName = groupName;
        this.username = username;
    }

    public String getUsername(){
        return this.username;
    }

    public String getGroupName(){
        return this.groupName;
    }
}
