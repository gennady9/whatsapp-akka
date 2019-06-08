package whatsapp.client;

// Package internal imports
import whatsapp.common.*;
// import whatsapp.server.ManagingServer;
import whatsapp.client.ClientMessages.*;
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
// import java.util.*;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;



public class User extends AbstractActor {

  // User actor fields
  String username;
  ActorRef inviter_ref;
  String group_invited_to;

  final ActorSelection managerServer = 
    getContext().actorSelection("akka://whatsapp@127.0.0.1:3553/user/managingServer");
    
  final static Timeout timeout_time = new Timeout(Duration.create(2, TimeUnit.SECONDS));
  LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

// Props
  static public Props props() {
    return Props.create(User.class, User::new);
  }
  //  ------------createReceive -- handeling client->user actor messages------------ 
  @Override
  public Receive createReceive() {
    return receiveBuilder()
        // Client->User messages located at ClientMessages.java
        // Client->User-related messages
        .match(ClientConnectMessage.class, x -> connectUser(x.username))
        .match(ClientDisconnectMessage.class, x -> disconnectUser())
        .match(ClientSendText.class, x -> sendUserText(x.target_name, x.text))
        .match(ClientSendFile.class, x -> sendUserFile(x.target_name, x.file))
        // Client->Group-related messages
        .match(ClientGroupCreate.class, x -> createGroup(x.group_name))
        .match(ClientGroupLeave.class, x -> leaveGroup(x.group_name))
        .match(ClientGroupText.class, x -> sendGroupText(x.group_name, x.text))
        .match(ClientGroupFile.class, x -> sendGroupFile(x.group_name, x.file))
        .match(ClientGroupInvite.class, x -> inviteToGroup(x.group_name, x.target_name))
        .match(ClientGroupUserRem.class, x -> removeFromGroup(x.group_name, x.target_name))
        .match(ClientGroupUserMute.class, x -> muteUser(x.group_name, x.target_name, x.mute_time))
        .match(ClientGroupUserUnmute.class, x -> unmuteUser(x.group_name, x.target_name))
        .match(ClientGroupAddCoAdmin.class, x -> addCoAdmin(x.group_name, x.target_name))
        .match(ClientGroupRemCoAdmin.class, x -> remCoAdmin(x.group_name, x.target_name))
        // User-related messages
        .match(UserLogMessage.class,  x -> { p(x.getMessage()); })
        .match(UserTextMessage.class, x -> { p(x.getMessage()); })
        .match(UserFileMessage.class, x -> printFile(x.source, x.file))
        // Invite-related
        .match(UpdateTargetAboutInvite.class, x -> gotInvited(x.group_name))
        .match(ClientInviteAccepted.class, x -> inviteAccepted())
        .match(ClientInviteDeclined.class, x -> inviteDeclined())
        .match(UserInviteAccept.class, x -> handleInviteAccepted(x.group_name, x.accepter_name))
        // Server->User messages
        .match(ActionSuccess.class, x -> p(x.getMessage()))
        .match(ActionFailed.class, x -> p(x.getError()))
        .match(GroupTextMessage.class, x -> p(x.getMessage()))
        .match(GroupFileMessage.class, x -> printFile(x.username, x.file))
        .match(InviteUserMessage.class, x -> sendInviteToTarget(x.getGroupName(), x.getTargetUser()))
        .match(RemoveUserFromGroupMessage.class, x -> handleRemoveTarget(x.getGroupName(), x.getTargetActor()))
        .match(MuteUserMessage.class, x -> handleMuteTarget(x.getGroupName(), x.getTargetActor(), x.getSeconds()))
        .match(UnmuteUserMessage.class, x -> handleUnmuteTarget(x.getGroupName(), x.getTargetActor()))
        .build();
  }

//  ------------createReceive Client->User functions------------ 
    private void connectUser(String username){
      if(username.length() < 1) { p("Already connected as " + username); }
      Future<Object> future = Patterns.ask(managerServer, new ConnectMessage(username, getSelf()), timeout_time); 
      try {
        Object res = Await.result(future, timeout_time.duration());
        if(res instanceof ActionSuccess){
          this.username = username;
          p(((ActionSuccess)res).getMessage());
        } else if(res instanceof ActionFailed){
          p(((ActionFailed)res).getError());
        }else{
          p("Server is offline!");
        }
      }catch(Exception error){
        p("server is offline!");
      }
    }
  
    private void disconnectUser(){
      if(username == "") { p("Can't disconnect if not connected"); }
      Future<Object> future = Patterns.ask(managerServer, new DisconnectMessage(username), timeout_time); 
      try {
        Object res = Await.result(future, timeout_time.duration());
        if(res instanceof ActionSuccess){
          p(((ActionSuccess)res).getMessage());
          username = "";
        } else if(res instanceof ActionFailed){
          p(((ActionFailed)res).getError());
        }
        else{
          p("server is offline! try again later!");
        }
      }catch(Exception error){
        p("server is offline! try again later!");
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



  // ------------invite handle---------------- 

    private void sendInviteToTarget(String group_name, String target_name){
      ActorRef targetActorRef = getTargetRef(target_name);
      if(targetActorRef == null) { return; }
      targetActorRef.tell(new UpdateTargetAboutInvite(group_name, username), getSelf());
    }

    // Handle this user getting invited to specific group
    private void gotInvited(String group_name){ // User B
      p("You have been invited to " + group_name + ", Accept?");
      inviter_ref = getSender();
      group_invited_to = group_name;      
    }
    private void inviteAccepted(){ // User B
      if(inviter_ref == null || group_invited_to == null) 
        return;
      inviter_ref.tell(new UserInviteAccept(group_invited_to, username), getSelf());
      inviter_ref = null;
      group_invited_to = null;
    }

    private void inviteDeclined(){ // User B
      inviter_ref = null;
      group_invited_to = null;
    }

    private void handleInviteAccepted(String group_name, String accepter_name){ // User A
      managerServer.tell(new InviteUserApproveMessage(group_name, accepter_name), getSelf());
      getSender().tell(new UserLogMessage("Welcome to " + group_name + "!"), getSelf());
    }

  // ------------createReceive group-related---------------- 
  private void createGroup(String group_name){
    managerServer.tell(new CreateGroupMessage(group_name, username), getSelf());
  }

  private void leaveGroup(String group_name){
    managerServer.tell(new LeaveGroupMessage(group_name, username), getSelf());
  }

  private void sendGroupText(String group_name, String text){
    managerServer.tell(new GroupTextMessage(username, group_name, text), getSelf());
  }

  private void sendGroupFile(String group_name, byte[] file){
    managerServer.tell(new GroupFileMessage(username, group_name, file), getSelf());
  }

  private void inviteToGroup(String group_name, String target_name){
    managerServer.tell(new InviteUserMessage(group_name, target_name, username), getSelf());
  }

  private void removeFromGroup(String group_name, String target_name){
    managerServer.tell(new RemoveUserFromGroupMessage(group_name, username, target_name), getSelf());
  }

  private void handleRemoveTarget(String group_name, ActorRef removed_user){ // Admin message to removed user
    String message = "You have been removed from "+group_name+" by "+username+"!";
    String tagged_message = (getTime() + "["+ group_name +"][" + username + "]" + message);
    removed_user.tell(new UserLogMessage(tagged_message), getSelf());
  }

  private void muteUser(String group_name, String target_name, int mute_time){
    managerServer.tell(new MuteUserMessage(group_name, username, target_name, mute_time), getSelf());
  }
  private void handleMuteTarget(String group_name, ActorRef muted_user, int seconds){ // Admin message to removed user
    String message = "You have been muted for "+seconds+" in "+group_name+" by "+username+"!";
    String tagged_message = (getTime() + "["+ group_name +"][" + username + "]" + message);
    muted_user.tell(new UserLogMessage(tagged_message), getSelf());
  }

  private void unmuteUser(String group_name, String target_name){
    managerServer.tell(new UnmuteUserMessage(username, target_name, group_name), getSelf());
  }
  private void handleUnmuteTarget(String group_name, ActorRef muted_user){ // Admin message to removed user
    String message = "You have been unmuted in "+group_name+" by "+username+"!";
    String tagged_message = (getTime() + "["+ group_name +"][" + username + "]" + message);
    muted_user.tell(new UserLogMessage(tagged_message), getSelf());
  }

  private void addCoAdmin(String group_name, String target_name){
    managerServer.tell(new AddCoAdminMessage(group_name, username, target_name), getSelf());
  }

  private void remCoAdmin(String group_name, String target_name){
    managerServer.tell(new RemoveCoAdminMessage(group_name, username, target_name), getSelf());
  }
  // ------------createReceive Assisting methods------------ 
  private ActorRef getTargetRef(String target_name){
    Future<Object> future = Patterns.ask(managerServer, new GetUserDestMessage(target_name), timeout_time);
    ActorRef targetRef = null;
    try { 
      Object res = Await.result(future, timeout_time.duration());
      if(res instanceof ActionFailed){
        p(((ActionFailed)res).getError());
      }else{
        targetRef = (ActorRef) Await.result(future, timeout_time.duration()); 
      }
    }catch(Exception error){ p("server is offline!"); return null;}
    if(targetRef == ActorRef.noSender()){
      targetRef = null; // for doing nothing
      // p("server is offline! (Target received noSender)");
    }
    return targetRef;
  }

  private void printFile(String source, byte[] file_data){
    try{
      Path path = Paths.get("whatsapp-file");
      Files.write(path, file_data);
      p(getTime() + "[user][" + source + "]" + " File received: " + path);
    }catch (IOException error) { return; }
  }

  static public String getTime(){
    LocalDateTime now = LocalDateTime.now();
    return ("["+now.getHour()+":"+now.getMinute()+"]");
  }

  private static void p(String s){
    System.out.println(s);
  }

}