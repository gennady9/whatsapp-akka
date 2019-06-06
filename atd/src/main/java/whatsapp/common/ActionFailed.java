package whatsapp.common;

import java.io.Serializable;
import akka.actor.ActorRef;

public class ActionFailed implements Serializable {
    final String message;

    public ActionFailed(String message) {
        this.message = message;
    }

    public String getError() {
        return this.message;
    }
}
