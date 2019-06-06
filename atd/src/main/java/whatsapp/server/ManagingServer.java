package whatsapp.server;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import java.util.HashMap;
import java.util.Optional;
import akka.actor.ActorRef;
import whatsapp.common.ConnectMessage;
import whatsapp.common.DisconnectMessage;
import whatsapp.common.ActionFailed;
import whatsapp.common.ActionSuccess;

public class ManagingServer extends AbstractActor {
    private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private HashMap<String, ActorRef> connectedUsers;

    public ManagingServer() {
        this.connectedUsers = new HashMap<String, ActorRef>();
    }

    static public Props props() {
        return Props.create(ManagingServer.class, ManagingServer::new);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(ConnectMessage.class, this::connectUser)
            .match(DisconnectMessage.class, this::disconnectUser)
            .build();
    }

    private void connectUser(ConnectMessage connectMessage) {
        String message;
        if (this.connectedUsers.containsKey(connectMessage.getUsername())) {
            message = String.format("%s already exists.", connectMessage.getUsername());
            getSender().tell(new ActionFailed(
                    message), getSelf());
        } else {
            this.connectedUsers.put(connectMessage.getUsername(), connectMessage.getUserActor());
            message = String.format("%s connected successfully.", connectMessage.getUsername());
            getSender().tell(new ActionSuccess(message), getSelf());
        }

        log.info(message);
    }

    private void disconnectUser(DisconnectMessage disconnectMessage) {
        // TODO:: leave groups
        String message;
        if (this.connectedUsers.containsKey(disconnectMessage.getUsername())) {
            this.connectedUsers.remove(disconnectMessage.getUsername());
            message = String.format("%s disconnected successfully.", disconnectMessage.getUsername());
            getSender().tell(new ActionSuccess(
                    message), getSelf());
        } else {
            message = String.format("%s failed to disconnected.", disconnectMessage.getUsername());
            getSender().tell(new ActionFailed(
                    message), getSelf());
        }

        log.info(message);
    }
}