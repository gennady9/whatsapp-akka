package whatsapp.server;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import whatsapp.common.ActionFailed;
import whatsapp.common.ActionSuccess;
import whatsapp.common.CreateGroupMessage;
import whatsapp.common.GroupTextMessage;
import whatsapp.common.GroupFileMessage;
import whatsapp.common.MuteUserMessage;
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
                .match(MuteUserMessage.class, this::muteUser).build();
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

    private void createGroup(CreateGroupMessage createGroupMessage) {
        if (createGroupMessage.getAdmin() != this.admin || createGroupMessage.getName() != this.groupName) {
            return;
        }

        this.users.add(this.admin);
        this.router = this.router.addRoutee(new ActorRefRoutee(getSender()));

        this.router.route(new ActionSuccess(String.format("Group %s created successfully.", this.groupName)),
                getSelf());
    }

    private void muteUser(MuteUserMessage message) {
        String username = message.getUsername(); // TODO::check this mess..
        // admin check...
        if (this.admin != username && !this.coAdmins.contains(username)) {
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

        this.getContext().getSystem().scheduler().scheduleOnce(Duration.ofMillis(message.getSeconds() * 1000), () -> {
            if (this.mutedUsers.contains(target)) {
                this.mutedUsers.remove(target);
                message.getTargetActor().tell(
                        new GroupTextMessage(username, this.groupName, "You have been unmuted! Muting time is up!"),
                        getSelf());
            }
            // this.router = this.router.addRoutee(new
            // ActorRefRoutee(message.getTargetActor()));
        }, this.getContext().getSystem().dispatcher());

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