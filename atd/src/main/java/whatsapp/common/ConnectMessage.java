package whatsapp.common;

import java.io.Serializable;
import akka.actor.ActorRef;

public class ConnectMessage implements Serializable {
    final String username;
    final ActorRef userActor;

    public ConnectMessage(String username, ActorRef actor) {
        this.username = username;
        this.userActor = actor;
    }

    public String getUsername() {
        return this.username;
    }
    public ActorRef getUserActor() {
        return this.userActor;
    }
}
