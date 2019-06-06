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
import whatsapp.common.CreateGroupMessage;
import whatsapp.common.GroupTextMessage;
import whatsapp.common.GroupFileMessage;
import akka.routing.ActorRefRoutee;
import akka.routing.BroadcastRoutingLogic;
import akka.routing.Router;
import java.util.List;
import java.util.ArrayList;

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
    }

    static public Props props(String name, String admin) {
        return Props.create(GroupActor.class, ()-> new GroupActor(name, admin));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(CreateGroupMessage.class, this::createGroup)
            .match(GroupTextMessage.class, msg -> router.route(msg, getSelf()))
            .match(GroupFileMessage.class, msg -> router.route(msg, getSelf()))
            .build();
    }

    private void createGroup(CreateGroupMessage createGroupMessage) {
        if (createGroupMessage.getAdmin() != this.admin || createGroupMessage.getName() != this.groupName) {
            return;
        }

        this.users.add(this.admin);
        this.router = this.router.addRoutee(new ActorRefRoutee(getSender()));

        this.router.route(new ActionSuccess(String.format("Group %s created successfully.", this.groupName)), getSelf());
    }
}