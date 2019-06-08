package whatsapp.server;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import whatsapp.common.ActionFailed;
import whatsapp.common.ActionSuccess;
import whatsapp.common.AddCoAdminMessage;
import whatsapp.common.AutoUnmuteUserMessage;
import whatsapp.common.CreateGroupMessage;
import whatsapp.common.DeleteGroupMessage;
import whatsapp.common.DisconnectMessage;
import whatsapp.common.GroupTextMessage;
import whatsapp.common.InviteUserApproveMessage;
import whatsapp.common.InviteUserMessage;
import whatsapp.common.LeaveGroupMessage;
import whatsapp.common.GroupFileMessage;
import whatsapp.common.MuteUserMessage;
import whatsapp.common.RemoveCoAdminMessage;
import whatsapp.common.RemoveUserFromGroupMessage;
import whatsapp.common.UnmuteUserMessage;
import akka.routing.ActorRefRoutee;
import akka.routing.BroadcastRoutingLogic;
import akka.routing.Router;
import java.util.List;
import java.util.ArrayList;
import java.time.Duration;

public class GroupActor extends AbstractActor {
    private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    Router router;
    private String groupName;
    private String admin;
    private List<String> users;
    private List<String> coAdmins;
    private List<String> mutedUsers;

    public GroupActor(String name, String admin) {
        this.groupName = name;
        this.admin = admin;
        this.users = new ArrayList<String>();
        this.coAdmins = new ArrayList<String>();
        this.mutedUsers = new ArrayList<String>();
        this.router = new Router(new BroadcastRoutingLogic());
    }

    static public Props props(String name, String admin) {
        return Props.create(GroupActor.class, () -> new GroupActor(name, admin));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(CreateGroupMessage.class, this::createGroup)
                .match(GroupTextMessage.class, (GroupTextMessage msg) -> broadcastMessage(msg.getUsername(), msg))
                .match(GroupFileMessage.class, (GroupFileMessage msg) -> broadcastMessage(msg.getUsername(), msg))
                .match(MuteUserMessage.class, this::muteUser).match(LeaveGroupMessage.class, this::leaveGroup)
                .match(InviteUserMessage.class, this::inviteUser)
                .match(InviteUserApproveMessage.class,
                        (message) -> addUserToGroup(message.getUsername(), message.getTargetActor()))
                .match(AutoUnmuteUserMessage.class,
                        message -> unmuteUser(message.getUsername(), message.getTarget(), message.getTargetActor(),
                                true))
                .match(UnmuteUserMessage.class,
                        message -> unmuteUser(message.getUsername(), message.getTarget(), message.getTargetActor(),
                                false))
                .match(RemoveUserFromGroupMessage.class, this::removeUser)
                .match(AddCoAdminMessage.class, this::addCoadmin).match(RemoveCoAdminMessage.class, this::removeCoadmin)
                .match(DisconnectMessage.class, message -> deleteUserFromGroup(message.getUsername(), getSender()))
                .build();
    }

    private void addCoadmin(AddCoAdminMessage message) {
        if (!this.users.contains(message.getTarget())) {
            getSender().tell(new ActionFailed(String.format("%s does not exist!", message.getTarget())), getSelf());
            return;
        }

        if (!this.hasAdminPerms(message.getUsername())) {
            getSender().tell(
                    new ActionFailed(String.format("You are neither an admin nor a co-admin of %s!", this.groupName)),
                    getSelf());
            return;
        }

        this.coAdmins.add(message.getTarget());

        message.getTargetActor().tell(new GroupTextMessage(message.getUsername(), this.groupName,
                String.format("You have been promoted to co-admin in %s!", this.groupName)), getSelf());
    }

    private void removeCoadmin(RemoveCoAdminMessage message) {
        if (!this.users.contains(message.getTarget())) {
            getSender().tell(new ActionFailed(String.format("%s does not exist!", message.getTarget())), getSelf());
            return;
        }

        if (!this.hasAdminPerms(message.getUsername())) {
            getSender().tell(
                    new ActionFailed(String.format("You are neither an admin nor a co-admin of %s!", this.groupName)),
                    getSelf());
            return;
        }

        this.coAdmins.remove(message.getTarget());

        message.getTargetActor().tell(new GroupTextMessage(message.getUsername(), this.groupName,
                String.format("You have been demoted to user in %s!", this.groupName)), getSelf());
    }

    private void unmuteUser(String username, String target, ActorRef targetRef, boolean isAuto) {
        if (!this.mutedUsers.contains(target)) {
            if (!isAuto) {
                getSender().tell(new ActionFailed(String.format("%s is not muted!", target)), getSelf());
            }
            return;
        }

        this.mutedUsers.remove(target);
        targetRef.tell(new GroupTextMessage(username, this.groupName, "You have been unmuted! Muting time is up!"),
                getSelf());
    }

    private void removeUser(RemoveUserFromGroupMessage message) {
        String user = message.getUsername();
        if (!this.hasAdminPerms(user)) {
            getSender().tell(
                    new ActionFailed(String.format("You are neither an admin nor a co-admin of %s!", this.groupName)),
                    getSelf());
            return;
        }

        if (!this.users.contains(message.getTarget())) {
            getSender().tell(new ActionFailed(String.format("%s is not in %s!", message.getTarget(), this.groupName)),
                    getSelf());
            return;
        }

        deleteUserFromGroup(message.getTarget(), message.getTargetActor());

        getSender().tell(message, getSelf());
    }

    private boolean hasAdminPerms(String username) {
        return this.admin.equals(username) || this.coAdmins.contains(username);
    }

    private void inviteUser(InviteUserMessage message) {
        String user = message.getUsername();
        if (!this.hasAdminPerms(user)) {
            getSender().tell(
                    new ActionFailed(String.format("You are neither an admin nor a co-admin of %s!", this.groupName)),
                    getSelf());
            return;
        }

        String target = message.getTargetUser();
        if (users.contains(target)) {
            getSender().tell(new ActionFailed(String.format("%s is already in %s", target, this.groupName)), getSelf());
            return;
        }

        getSender().tell(message, getSelf());
    }

    private void leaveGroup(LeaveGroupMessage message) {
        String username = message.getUsername();
        if (!users.contains(username)) {
            getSender().tell(new ActionFailed(String.format("%s is not in %s", username, this.groupName)), getSelf());
            return;
        }
        deleteUserFromGroup(username, getSender());
        router.route(new ActionFailed(String.format("%s has left %s!", username, this.groupName)), getSelf());

    }

    private void deleteUserFromGroup(String username, ActorRef actorRef) {
        if (this.admin.equals(username)) {
            router.route(new GroupTextMessage(username, this.groupName,
                    String.format("%s admin has closed %s!", this.groupName, this.groupName)), getSelf());
            getContext().parent().tell(new DeleteGroupMessage(groupName), getSelf());
            return;
        }

        router = router.removeRoutee(actorRef);
        this.users.remove(username);
        this.coAdmins.remove(username);
        this.mutedUsers.remove(username);
    }

    private void broadcastMessage(String username, Object msg) {
        if (!this.users.contains(username)) {
            getSender().tell(new ActionFailed(String.format("You are not part of %s!", this.groupName)), getSelf());
            return;
        } else if (this.mutedUsers.contains(username)) {
            getSender().tell(new ActionFailed(String.format("You are muted in %s", this.groupName)), getSelf());
            return;
        }

        router.route(msg, getSelf());
    }

    private void addUserToGroup(String username, ActorRef actor) {
        this.users.add(username);
        this.router = this.router.addRoutee(new ActorRefRoutee(actor));
    }

    private void createGroup(CreateGroupMessage createGroupMessage) {
        if (!createGroupMessage.getAdmin().equals(this.admin) || !createGroupMessage.getName().equals(this.groupName)) {
            return;
        }

        addUserToGroup(this.admin, getSender());

        System.out.println("Server: group= '" + this.groupName + "' created successfully by user=" + this.admin);
        this.router.route(new ActionSuccess(String.format("Group %s created successfully.", this.groupName)),
                getSelf());
    }

    private void muteUser(MuteUserMessage message) {
        String username = message.getUsername(); // TODO::check this mess..
        // admin check...
        if (!this.admin.equals(username) && !this.coAdmins.contains(username)) {
            getSender().tell(new ActionFailed(
                    String.format("You are neither an admin nor a co-admin of %s! does not exist!", this.groupName)),
                    getSelf());

            return;
        }

        String target = message.getTarget();

        if (!this.mutedUsers.contains(target)) {
            this.mutedUsers.add(target);
        }

        // this.router = this.router.removeRoutee(message.getTargetActor());

        this.getContext().getSystem().scheduler().scheduleOnce(Duration.ofMillis(message.getSeconds() * 1000),
                getSelf(), new AutoUnmuteUserMessage(username, target, message.getTargetActor(), this.groupName),
                this.getContext().getSystem().dispatcher(), getSender());

        message.getTargetActor()
                .tell(new GroupTextMessage(username, this.groupName, String.format(
                        "You have been muted for %d in %s by %s!", message.getSeconds(), this.groupName, username)),
                        getSelf());

        // this.getContext().getSystem().scheduler()
        // .scheduleOnce(
        // Duration.ofMillis(message.getSeconds() * 1000),
        // () -> {
        // if(this.mutedUsers.contains(target)){
        // this.mutedUsers.remove(target);
        // }

        // this.router = this.router.addRoutee(new
        // ActorRefRoutee(message.getTargetActor()));
        // message.getTargetActor().tell(new GroupTextMessage(username, this.groupName,
        // "You have been unmuted! Muting time is up!"),
        // getSelf());
        // }
        // ,
        // this.getContext().getSystem().dispatcher());

    }
}