package whatsapp.common;

import java.io.Serializable;
import akka.actor.ActorRef;

public class ActionSuccess implements Serializable {
    final String message;

    public ActionSuccess(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }
}
