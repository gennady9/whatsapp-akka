package whatsapp.common;

import java.io.Serializable;
import akka.actor.ActorRef;

public class CreateGroupMessage implements Serializable {
    final String admin;
    final String name;

    public CreateGroupMessage(String name, String admin) {
        this.name = name;
        this.admin = admin;
    }

    public String getAdmin() {
        return this.admin;
    }

    public String getName() {
        return this.name;
    }
}
