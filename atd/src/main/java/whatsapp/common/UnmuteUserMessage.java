package whatsapp.common;

import java.io.Serializable;
import akka.actor.ActorRef;

public class UnmuteUserMessage implements Serializable {
    final String userName;
    final String target;
    final String groupName;
    private ActorRef targetActor;

    public UnmuteUserMessage(String userName, String target, String groupName) {
        this.userName = userName;
        this.target = target;
        this.targetActor = null;
        this.groupName = groupName;
    }

    public void setTargetActor(ActorRef actor) {
        this.targetActor = actor;
    }

    public String getUsername() {
        return this.userName;
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