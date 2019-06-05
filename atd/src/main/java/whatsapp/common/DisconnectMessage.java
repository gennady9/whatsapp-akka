package whatsapp.common;

import java.io.Serializable;
import akka.actor.ActorRef;

public class DisconnectMessage implements Serializable {
    final String username;

    public DisconnectMessage(String username) {
        this.username = username;
    }

    public String getUsername() {
        return this.username;
    }
}
