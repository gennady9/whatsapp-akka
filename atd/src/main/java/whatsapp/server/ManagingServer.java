package whatsapp.server;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import java.util.HashMap;
import akka.actor.ActorRef;
import whatsapp.common.ConnectMessage;
import whatsapp.common.DisconnectMessage;

public class ManagingServer extends AbstractActor {
    private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private HashMap<String, ActorRef> connectedUsers;

    static public Props props() {
        return Props.create(ManagingServer.class, () -> new ManagingServer());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(ConnectMessage.class, this::connectUser)
            .match(DisconnectMessage.class, this::disconnectUser)
            .build();
    }

    private void connectUser(ConnectMessage connectMessage) {
        log.info(connectMessage.getUsername()); //TODO
    }

    private void disconnectUser(DisconnectMessage disconnectMessage) {
        log.info(disconnectMessage.getUsername()); //TODO
    }
}