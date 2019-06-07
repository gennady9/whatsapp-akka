package whatsapp.common;

import java.io.Serializable;

import akka.actor.ActorRef;

public class InviteUserMessage implements Serializable {
    final String groupName;
    final String target;
    final String username;
    private ActorRef targetActor;

    public InviteUserMessage(String groupName, String target, String username) {
        this.groupName = groupName;
        this.username = username;
        this.target = target;
    }

    public String getUsername(){
        return this.username;
    }

    public String getGroupName(){
        return this.groupName;
    }

    public String getTargetUser(){
        return this.target;
    }
}
