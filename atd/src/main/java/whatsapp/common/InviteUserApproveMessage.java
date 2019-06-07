package whatsapp.common;

import java.io.Serializable;

import akka.actor.ActorRef;

public class InviteUserApproveMessage implements Serializable {
    final String groupName;
    final String username;
    private ActorRef targetActor;

    public InviteUserApproveMessage(String groupName, String username) {
        this.groupName = groupName;
        this.username = username;
        this.targetActor = null;
    }

    public void setTargetActor(ActorRef actor) {
        this.targetActor = actor;
    }

    public ActorRef getTargetActor() {
        return this.targetActor;
    }

    public String getUsername() {
        return this.username;
    }

    public String getGroupName() {
        return this.groupName;
    }
}
