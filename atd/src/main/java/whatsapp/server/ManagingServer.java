package whatsapp.server;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.Function;

import akka.actor.ActorRef;
import whatsapp.common.ConnectMessage;
import whatsapp.common.DisconnectMessage;
import whatsapp.common.ActionFailed;
import whatsapp.common.ActionSuccess;
import whatsapp.common.AddCoAdminMessage;
import whatsapp.common.LeaveGroupMessage;
import whatsapp.common.CreateGroupMessage;
import whatsapp.common.DeleteGroupMessage;
import whatsapp.common.GetUserDestMessage;
import whatsapp.common.GroupFileMessage;
import whatsapp.common.GroupTextMessage;
import whatsapp.common.InviteUserApproveMessage;
import whatsapp.common.InviteUserMessage;
import whatsapp.common.MuteUserMessage;
import whatsapp.common.RemoveCoAdminMessage;
import whatsapp.common.RemoveUserFromGroupMessage;
import whatsapp.common.UnmuteUserMessage;

public class ManagingServer extends AbstractActor {
    private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private HashMap<String, ActorRef> connectedUsers;
    private HashMap<String, ActorRef> groups;

    public ManagingServer() {
        this.connectedUsers = new HashMap<String, ActorRef>();
        this.groups = new HashMap<String, ActorRef>();
    }

    static public Props props() {
        return Props.create(ManagingServer.class, ManagingServer::new);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(ConnectMessage.class, this::connectUser)
                .match(DisconnectMessage.class, this::disconnectUser).match(CreateGroupMessage.class, this::createGroup)
                .match(LeaveGroupMessage.class, (message) -> handleGroupForward(message.getGroupName(), message))
                .match(GetUserDestMessage.class, this::getUserDest)
                .match(MuteUserMessage.class, this::handleMuteUser)
                .match(GroupFileMessage.class, (message) -> handleGroupForward(message.getGroupName(), message))
                .match(GroupTextMessage.class, (message) -> handleGroupForward(message.getGroupName(), message))
                .match(InviteUserMessage.class, this::inviteUser)
                .match(InviteUserApproveMessage.class, message -> handleGroupForward(message.getGroupName(), message,
                () -> message.setTargetActor(this.connectedUsers.get(message.getUsername()))))
                .match(DeleteGroupMessage.class, this::deleteGroup) // Recieves from GroupActor ..
                .match(RemoveUserFromGroupMessage.class, message -> handleGroupForward(message.getGroupName(), message,
                () -> message.setTargetActor(this.connectedUsers.get(message.getTarget()))))
                .match(UnmuteUserMessage.class,
                        message -> handleGroupForward(message.getGroupName(), message,
                                () -> message.setTargetActor(this.connectedUsers.get(message.getUsername()))))
                .match(RemoveCoAdminMessage.class,
                        message -> handleGroupForward(message.getGroupName(), message,
                                () -> message.setTargetActor(this.connectedUsers.get(message.getUsername()))))
                .match(AddCoAdminMessage.class, message -> handleGroupForward(message.getGroupName(), message,
                        () -> message.setTargetActor(this.connectedUsers.get(message.getUsername()))))
                .build();
    }

    private void inviteUser(InviteUserMessage message) {
        if (!this.connectedUsers.containsKey(message.getUsername())) {
            getSender().tell(new ActionFailed(String.format("%s does not exist!", message.getUsername())), getSelf());

            return;
        }

        handleGroupForward(message.getGroupName(), message);
    }

    private void createGroup(CreateGroupMessage createGroupMessage) {
        if (this.groups.containsKey(createGroupMessage.getName())) {
            System.out.println("Server: group '" + createGroupMessage.getName() + "' already exists"); // TODO: maybe
                                                                                                       // delete
            getSender().tell(new ActionFailed(String.format("%s already exists.", createGroupMessage.getName())),
                    getSelf());
            return;
        }

        ActorRef newGroupActor = getContext().actorOf(
                GroupActor.props(createGroupMessage.getName(), createGroupMessage.getAdmin()),
                createGroupMessage.getName());
        this.groups.put(createGroupMessage.getName(), newGroupActor);
        newGroupActor.forward(createGroupMessage, getContext());
    }

    private void getUserDest(GetUserDestMessage message) {
        if (!this.connectedUsers.containsKey(message.getUsername())) {
            getSender().tell(new ActionFailed(String.format("%s does not exist!", message.getUsername())),
                    getSelf());

            return;
        }

        ActorRef target = this.connectedUsers.get(message.getUsername());
        getSender().tell(target, getSelf());
    }

    private void deleteGroup(DeleteGroupMessage message) {
        ActorRef group = this.groups.remove(message.getGroupName());
        this.getContext().stop(group);
    }

    private void handleGroupForward(String groupName, Object message) {
        if (!this.groups.containsKey(groupName)) {
            getSender().tell(new ActionFailed(String.format("%s does not exist!", groupName)), getSelf());

            return;
        }

        groups.get(groupName).forward(message, getContext());
    }

    private void handleGroupForward(String groupName, Object message, Runnable setup) {
        setup.run();
        this.handleGroupForward(groupName, message);
    }

    private void handleMuteUser(MuteUserMessage message) {
        String groupName = message.getGroupName();
        String target = message.getTarget();
        if (!this.groups.containsKey(groupName)) {
            getSender().tell(new ActionFailed(String.format("%s does not exist!", groupName)), getSelf());

            return;
        } else if (!this.connectedUsers.containsKey(target)) {
            getSender().tell(new ActionFailed(String.format("%s does not exist!", target)), getSelf());

            return;
        }

        message.setTargetActor(this.connectedUsers.get(target));

        groups.get(groupName).forward(message, getContext());
    }

    private void connectUser(ConnectMessage message) {
        if (this.connectedUsers.containsKey(message.getUsername())) {
            getSender().tell(new ActionFailed(String.format("%s already exists.", message.getUsername())), getSelf());

            return;
        }

        this.connectedUsers.put(message.getUsername(), message.getUserActor());
        getSender().tell(new ActionSuccess(String.format("%s connected successfully.", message.getUsername())), getSelf());
    }

    private void disconnectUser(DisconnectMessage message) {
        if (!this.connectedUsers.containsKey(message.getUsername())) {
            getSender().tell(new ActionFailed(String.format("%s failed to disconnected.", message.getUsername())), getSelf());

            return;
        }

        this.connectedUsers.remove(message.getUsername());
        getSender().tell(new ActionSuccess(String.format("%s disconnected successfully.", message.getUsername())), getSelf());
        groups.values().forEach(ref -> ref.forward(message, getContext()));
    }
}