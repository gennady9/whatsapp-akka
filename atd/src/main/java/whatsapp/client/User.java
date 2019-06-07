package whatsapp.client;

// Package internal imports
import whatsapp.common.*;
/*
import whatsapp.common.ConnectMessage;
import whatsapp.common.DisconnectMessage;
import whatsapp.common.GetUserDestMessage;
import whatsapp.common.ActionFailed;
import whatsapp.common.ActionSuccess;
*/
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
import java.time.LocalDateTime;
// import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.io.IOException;



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
  // Client-related
  static public class ClientConnectMessage {
    public final String username;
    public ClientConnectMessage(String username) {
        this.username = username;
    }
  }
  static public class ClientDisconnectMessage {
    public ClientDisconnectMessage() {}
  }

  static public class ClientSendText{
    String target_name;
    String text;
    public ClientSendText(String target_name, String text){
      this.target_name = target_name;
      this.text = text;
    }
  }
  static public class ClientSendFile{
    String target_name;
    byte[] file;
    public ClientSendFile(String target_name, byte[] file){
      this.target_name = target_name;
      this.file = file;
    }
  }


  static public class UserTextMessage implements Serializable {
    String source;
    String message;
    public UserTextMessage(String source, String message) {
        this.source = source;
        this.message = message;
    }
    public String getMessage() { // TODO: maybe eval time at message creation and not at "get"?
        return (getTime() + "[user][" + source + "]" + message);
    }
  }

  static public class UserFileMessage implements Serializable {
    String source;
    byte[] file;
    public UserFileMessage(String source, byte[] file) {
        this.source = source;
        this.file = file;
    }
    // public String getData() { // TODO: maybe eval time at message creation and not at "get"?
        // return (getTime() + "[user][" + source + "] File received: " + file);
  }

  // Group-related
  static public class ClientGroupCreate {
    public final String group_name;
    public ClientGroupCreate(String group_name) {
        this.group_name = group_name;
    }
  }
  
  static public class ClientGroupLeave {
    public final String group_name;
    public ClientGroupLeave(String group_name) {
        this.group_name = group_name;
    }
  }

  static public class ClientGroupText{
    String group_name;
    String text;
    public ClientGroupText(String group_name, String text){
      this.group_name = group_name;
      this.text = text;
    }
  }



  //  ------------createReceive - actions handeling------------ 
  @Override
  public Receive createReceive() {
    return receiveBuilder()
        // User-related
        .match(ClientConnectMessage.class, x -> connectUser(x.username))
        .match(ClientDisconnectMessage.class, x -> disconnectUser())
        .match(ClientSendText.class, x -> sendUserText(x.target_name, x.text))
        .match(ClientSendFile.class, x -> sendUserFile(x.target_name, x.file))
        .match(UserTextMessage.class, x -> { log.info(x.getMessage()); })
        .match(UserFileMessage.class, x -> printFile(x.source, x.file))
        // Group-related
        .match(ClientGroupCreate.class, x -> createGroup(x.group_name))
        .match(ClientGroupLeave.class, x -> leaveGroup(x.group_name))
        .match(ClientGroupText.class, x -> sendGroupText(x.group_name, x.text))
        // .match(ClientGroupFile.class, x -> sendGroupFile(x.group_name, x.text))
        // Actions received from server
        .match(ActionSuccess.class, x -> log.info(x.getMessage()))
        .match(ActionFailed.class, x -> log.info(x.getError()))
        .match(GroupTextMessage.class, x -> log.info(x.getMessage()))
        // .match(GroupFileMessage.class, x -> log.info(x.getMessage()))
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
  
    private void disconnectUser(){ // TODO: Area51
      Future<Object> future = Patterns.ask(managerServer, new DisconnectMessage(username), timeout_time); 
      try {
        Object res = Await.result(future, timeout_time.duration());
        if(res instanceof ActionSuccess){
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

    private void sendUserText(String target_name, String text){
      ActorRef targetActorRef = getTargetRef(target_name);
      if(targetActorRef == null) { return; }
      targetActorRef.tell(new UserTextMessage(username, text), getSelf());
    }
    private void sendUserFile(String target_name, byte[] file){
      ActorRef targetActorRef = getTargetRef(target_name);
      if(targetActorRef == null) { return; }
      targetActorRef.tell(new UserFileMessage(username, file), getSelf());
    }

    private void printFile(String source, byte[] file_data){
      try{
        Path path = Paths.get("whatsapp-file");
        Files.write(path, file_data);
        log.info(getTime() + "[user][" + source + "]" + "File received: " + path);
      }catch (IOException error) {
        System.out.println("print file error = " + error);
      }
    }

  // ------------createReceive group-related---------------- 
  private void createGroup(String group_name){
    managerServer.tell(new CreateGroupMessage(group_name, username), getSelf());
  }

  private void leaveGroup(String group_name){
    managerServer.tell(new LeaveGroupMessage(group_name, username), getSelf());
  }

  private void sendGroupText(String group_name, String text){
    System.out.println("[debug] told manager about group text");
    managerServer.tell(new GroupTextMessage(username, group_name, text), getSelf());
  }
  // ------------createReceive Assisting methods------------ 
  private ActorRef getTargetRef(String target_name){
    Future<Object> future = Patterns.ask(managerServer, new GetUserDestMessage(target_name), timeout_time);
    ActorRef targetRef = null;
    try { targetRef = (ActorRef) Await.result(future, timeout_time.duration()); 
    }catch(Exception error){ log.info("server is offline!"); return null;}
    if(targetRef != null && targetRef == ActorRef.noSender()){
      targetRef = null; // for doing nothing
      System.out.println("server is offline! (Target received noSender)");
    }
    return targetRef;
  }

  static private String getTime(){
    LocalDateTime now = LocalDateTime.now();
    return ("["+now.getHour()+":"+now.getMinute()+"]");
  }
  

}



// Code graveyard R.I.P