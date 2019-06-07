package whatsapp.common;

import java.io.Serializable;

public class DeleteGroupMessage implements Serializable {
    final String groupName;

    public DeleteGroupMessage(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupName(){
        return this.groupName;
    }
}
