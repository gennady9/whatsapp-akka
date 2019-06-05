package whatsapp.client;

import akka.actor.AbstractActor;
// import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.ActorSelection;

//#User-messages
public class User extends AbstractActor {
//#User-messages
  static public Props props(/*String userName, ActorRef serverActor*/) {
    return Props.create(User.class, () -> new User(/*"userName", serverActor*/));
  }


  static public class Connect {
    public Connect() {
    }
  }
  //#user-messages
//   private final String userName;
//   private final ActorRef serverActor;

  public User(/*String userName, ActorRef serverActor*/) {
    // this.userName = userName;
    // this.serverActor = serverActor;
  }

  
  @Override
  public Receive createReceive() {
    return receiveBuilder()
  //       .match(Connect.class, x -> {
  //         //#connect-send-message
  //         System.out.println("matched with connect");
  //         ActorSelection selection =
  //         getContext().actorSelection("akka.tcp://app@10.0.0.1:2552/whatsapp/server/Server");            
  //         selection.tell(new connectUser("test", getSelf()), getSelf());
  //         //#connect-send-message
  //       })
        .build();
  }

}

