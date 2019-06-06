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
import whatsapp.common.LeaveGroupMessage;
import whatsapp.common.CreateGroupMessage;
import whatsapp.common.GetUserDestMessage;
import whatsapp.common.GroupFileMessage;
import whatsapp.common.GroupTextMessage;
import whatsapp.common.MuteUserMessage;

public class ManagingServer extends AbstractActor {
    private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private HashMap<String, ActorRef> connectedUsers;
    private HashMap<String, ActorRef> groups;

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
            .match(CreateGroupMessage.class, this::createGroup)
            .match(LeaveGroupMessage.class, this::leaveGroup)
            .match(GetUserDestMessage.class, this::getUserDest)
            .match(MuteUserMessage.class, this::handleMuteUser)
            .match(GroupFileMessage.class, (message) -> handleGroupForward(message.getUsername(), message))
            .match(GroupTextMessage.class, (message) -> handleGroupForward(message.getUsername(), message))
            .build();
    }

    private void createGroup(CreateGroupMessage createGroupMessage) {
        if (this.groups.containsKey(createGroupMessage.getName())) {
            getSender().tell(new ActionFailed(
                    String.format("%s already exists.", createGroupMessage.getName())), getSelf());

            return;
        }

        ActorRef newGroupActor = getContext().actorOf(GroupActor.props(createGroupMessage.getName(), createGroupMessage.getAdmin()), createGroupMessage.getName());
        this.groups.put(createGroupMessage.getName(), newGroupActor);
        newGroupActor.forward(createGroupMessage, getContext());
    }

    private void getUserDest(GetUserDestMessage getUserDestMessage) {
        if (!this.connectedUsers.containsKey(getUserDestMessage.getUsername())) {
            getSender().tell(new ActionFailed(
                    String.format("%s does not exist!", getUserDestMessage.getUsername())), getSelf());

            return;
        }

        ActorRef target = this.connectedUsers.get(getUserDestMessage.getUsername());
        getSender().tell(target, getSelf());
    }

    private void leaveGroup(LeaveGroupMessage leaveGroupMessage) {
        String user = leaveGroupMessage.getUsername(); // TODO::
        // if()
    }

    private void handleGroupForward(String groupName, Object message){
        if (!this.groups.containsKey(groupName)) {
            getSender().tell(new ActionFailed(
                    String.format("%s does not exist!", groupName)), getSelf());

            return;
        }

        groups.get(groupName).forward(message, getContext());
    }

    private void handleMuteUser(MuteUserMessage message){
        String groupName = message.getGroupName();
        String target = message.getTarget();
        if (!this.groups.containsKey(groupName)) {
            getSender().tell(new ActionFailed(
                    String.format("%s does not exist!", groupName)), getSelf());

            return;
        } else if (!this.connectedUsers.containsKey(target)) {
            getSender().tell(new ActionFailed(
                    String.format("%s does not exist!", target)), getSelf());

            return;
        }

        message.setTargetActor(this.connectedUsers.get(target));

        groups.get(groupName).forward(message, getContext());
    }

    private void connectUser(ConnectMessage connectMessage) {
        String message;
        if (this.connectedUsers.containsKey(connectMessage.getUsername())) {
            message = String.format("%s already exists.", connectMessage.getUsername());
            getSender().tell(new ActionFailed(
                    message), getSelf());

            log.info(message);

            return;
        }

        this.connectedUsers.put(connectMessage.getUsername(), connectMessage.getUserActor());
        message = String.format("%s connected successfully.", connectMessage.getUsername());
        getSender().tell(new ActionSuccess(message), getSelf());

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