package whatsapp.server;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.actor.ActorRef;
import akka.event.Logging;
import akka.event.LoggingAdapter;

// import java.util.*;

//#server-messages
public class Server extends AbstractActor {
//#server-messages
  static public Props props() {
    return Props.create(Server.class, () -> new Server());
  }

//   private final ActorRef connectedUserActor;
//   private final String userName;
  

  static public class connectUser {
    // public List<String> usersConnected = new ArrayList<String>();

    public connectUser(String userName, ActorRef userActor) {
      // List<String> newList = new ArrayList<String>(usersConnected); // TODO isn't functional, find a way
      // newList.add(userName);
      // this.usersConnected = newList;
    //   this.connectedUserActor = userActor;
    //   this.userName = userName;
    }
  }
  //#server-messages

  private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

  public Server() {
  }

  @Override
  public Receive createReceive() {
    return receiveBuilder()
        .match(connectUser.class, user -> {
            log.info("Some user connected to server");
        })
        .build();
  }
}
