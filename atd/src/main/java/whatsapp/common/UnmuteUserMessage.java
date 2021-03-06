package whatsapp.common;

import java.io.Serializable;
import akka.actor.ActorRef;

public class UnmuteUserMessage implements Serializable {
    final String username;
    final String target;
    final String groupName;
    private ActorRef targetActor;

    public UnmuteUserMessage(String username, String target, String groupName) {
        this.username = username;
        this.target = target;
        this.targetActor = null;
        this.groupName = groupName;
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