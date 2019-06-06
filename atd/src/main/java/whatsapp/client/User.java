package whatsapp.client;

// Package internal imports
import whatsapp.common.ConnectMessage;
import whatsapp.common.DisconnectMessage;
import whatsapp.common.ActionFailed;
import whatsapp.common.ActionSuccess;
import whatsapp.server.ManagingServer;
// Akka imports
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.ActorSelection;
import akka.pattern.Patterns;
import akka.util.Timeout;
import scala.concurrent.duration.Duration;
import scala.concurrent.Await;
import scala.concurrent.Future;
import akka.event.Logging;
import akka.event.LoggingAdapter;
// Java imports
import java.util.HashMap;
import java.io.Serializable;
// import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


public class User extends AbstractActor {

  // User actor fields
  String username;
  // HashMap<String, ActorRef> groups = new HashMap<String, ActorRef>();
  final ActorSelection managerServer = 
    getContext().actorSelection("akka://whatsapp@127.0.0.1:3553/user/managingServer");
    
  final static Timeout timeout_time = new Timeout(Duration.create(1, TimeUnit.SECONDS));
  LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

// Props
  static public Props props() {
    return Props.create(User.class, User::new); // TODO: new User or User::new? whats the difference
  }

  // ------------Behaviours / "Methods"------------
  static public class ClientConnectMessage {
    public final String username;
    public ClientConnectMessage(String username) {
        this.username = username;
    }
  }
  static public class ClientDisconnectMessage {
    public ClientDisconnectMessage() {}
  }

  //  ------------createReceive - actions handeling------------ 
  @Override
  public Receive createReceive() {
    return receiveBuilder()
        .match(ClientConnectMessage.class, x -> connectUser(x.username))
        .match(ClientDisconnectMessage.class, x -> disconnectUser())
        .build();
  }

    // handeling createReceive.match functions:
    private void connectUser(String username){// TODO: Area51
      Future<Object> future = Patterns.ask(managerServer, new ConnectMessage(username, getSelf()), timeout_time); 
      try {
        Object res = Await.result(future, timeout_time.duration());
        if(res instanceof ActionSuccess){
          this.username = username;
          ActionSuccess actionRes = (ActionSuccess)res;
          log.info(actionRes.getMessage());
        } else if(res instanceof ActionFailed){
          ActionFailed actionRes = (ActionFailed)res;
          log.info(actionRes.getError());
        }
        else{
          log.info("Connection failed!"); // TODO: maybe not needed + check if user is taken error works
        }
      }catch(Exception error){
        log.info("server is offline!");
      }
    }
  
    private void disconnectUser(){
      Future<Object> future = Patterns.ask(managerServer, new DisconnectMessage(username), timeout_time); 
      try {
        Object res = Await.result(future, timeout_time.duration());
        if(res instanceof ActionSuccess){
          this.username = username;
          ActionSuccess actionRes = (ActionSuccess)res;
          log.info(actionRes.getMessage());
        } else if(res instanceof ActionFailed){
          ActionFailed actionRes = (ActionFailed)res;
          log.info(actionRes.getError());
        }
        else{
          log.info("Connection failed!"); // TODO: maybe not needed + check if user is taken error works
        }
      }catch(Exception error){
        log.info("server is offline! try again later!");
      }
    }

}



// Code graveyard R.I.P

 // public User(/*String userName, ActorRef serverActor*/) { // TODO: probably not needed
    // this.userName = userName;
    // this.serverActor = serverActor;
 // }