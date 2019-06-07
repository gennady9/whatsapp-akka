package whatsapp.common;

import java.io.Serializable;
import akka.actor.ActorRef;

public class RemoveCoAdminMessage implements Serializable {
    final String groupName;
    final String username;
    final String target;
    private ActorRef targetActor;

    public RemoveCoAdminMessage(String groupName, String username, String target) {
        this.groupName = groupName;
        this.username = username;
        this.target = target;
        this.targetActor = null;
    }

    public void setTargetActor(ActorRef actor) {
        this.targetActor = actor;
    }

    public String getUsername() {
        return this.username;
    }

    public String getTarget() {
        return this.target;
    }

    public String getGroupName() {
        return this.groupName;
    }

    public ActorRef getTargetActor() {
        return this.targetActor;
    }
}