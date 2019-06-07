package whatsapp.common;

import java.io.Serializable;
import akka.actor.ActorRef;

public class AutoUnmuteUserMessage implements Serializable {
    final String userName;
    final String target;
    final ActorRef targetActor;
    final String groupName;

    public AutoUnmuteUserMessage(String userName, String target, ActorRef targetActor, String groupName) {
        this.userName = userName;
        this.target = target;
        this.targetActor = targetActor;
        this.groupName = groupName;
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