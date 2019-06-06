package whatsapp.common;

import java.io.Serializable;
import akka.actor.ActorRef;

public class MuteUserMessage implements Serializable {
    final String groupName;
    final String username;
    final String target;
    final int seconds;
    private ActorRef targetActor;

    public MuteUserMessage(String groupName, String username, String target, int seconds) {
        this.groupName = groupName;
        this.username = username;
        this.seconds = seconds;
        this.target = target;
        this.targetActor = null;
    }

    public void setTargetActor(ActorRef actor){
        this.targetActor = actor;
    }

    public ActorRef getTargetActor(){
        return this.targetActor;
    }

    public String getUsername(){
        return this.username;
    }

    public int getSeconds(){
        return this.seconds;
    }

    public String getTarget(){
        return this.target;
    }

    public String getGroupName(){
        return this.groupName;
    }
}
