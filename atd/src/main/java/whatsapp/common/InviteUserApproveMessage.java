package whatsapp.common;

import java.io.Serializable;

public class InviteUserApproveMessage implements Serializable {
    final String groupName;
    final String username;

    public InviteUserApproveMessage(String groupName, String username) {
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
