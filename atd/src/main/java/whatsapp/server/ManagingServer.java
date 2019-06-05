package whatsapp.server;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import java.util.HashMap;
import akka.actor.ActorRef;
import whatsapp.common.ConnectMessage;

public class ManagingServer extends AbstractActor {
    private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private HashMap<String, ActorRef> connectedUsers;

    static public Props props() {
        return Props.create(ManagingServer.class, () -> new ManagingServer());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(ConnectMessage.class, connectMessage -> {
                log.info(connectMessage.getUsername());
                })
            .build();
    }

}