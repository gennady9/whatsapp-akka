package whatsapp.client;
// package internal imports
import whatsapp.client.ClientMessages.*;
// Akka imports
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.typesafe.config.ConfigFactory;
// Java imports
import java.util.*;
import java.io.IOException;
import java.util.Scanner;
import java.nio.file.Files;
import java.nio.file.Paths;


public class Main {
  public static void main(String[] args) {
    final ActorSystem system = ActorSystem.create("client", ConfigFactory.load("client"));
    final ActorRef userActor = system.actorOf(User.props(), "userActor");
    //  ------------Main - input ------------ 

      Scanner scanner = new Scanner(System.in);
      try {
        String input;
        while(true){
            input = scanner.nextLine();
            if(input.startsWith("/user")){
              handle_user_cmd(userActor, input);
            }else if(input.startsWith("/group")){
              handle_group_cmd(userActor, input);
            }else if(input.toLowerCase().contains("yes")){
              accept_invite(userActor);
            }else if(input.startsWith("no")){ // if no, do nothing
              decline_invite(userActor);
            }else if(input.startsWith("exit")){
              break;
            }else{
              p("Unknown command: '" + input + "' ,Try again or type 'exit'");
            }
        }
    } finally {
      scanner.close();
      system.terminate();
    }
  }

    //  ------------ Handeling user-related commands ------------ 
  private static void handle_user_cmd(ActorRef userActor, String input){
    String[] input_array = input.split("\\s+");  // remove spaces and seperate
    // input array structure: [0] /user, [1] command
    String command = input_array[1];
    // handeling command
    if(command.equals("connect")  && input_array.length >= 3){
      userActor.tell(new ClientConnectMessage(
                        input_array[2]), // Username to be connected with
                        ActorRef.noSender());
    }else if(command.equals("disconnect")  && input_array.length >= 2){
      userActor.tell(new ClientDisconnectMessage(), ActorRef.noSender());
    // USER COMMUNICATION
    }else if(command.equals("text")  && input_array.length >= 4){
      userActor.tell(new ClientSendText(
                        input_array[2], // Target name
                        cutText(input_array, 3)),// Message text content
                        ActorRef.noSender());
    }else if(command.equals("file")){
      String path = input_array[3];
      byte[] file = readFile(path);
      if (file != null)
        userActor.tell(new ClientSendFile(
                        input_array[2], // Target name
                        file), ActorRef.noSender());
    }else{
      p("Unknown command: '" + input + "' ,Try again or type 'exit'");
    }
  }

  // invite special command
  private static void accept_invite(ActorRef userActor){
    userActor.tell(new ClientInviteAccepted(), ActorRef.noSender());
  }
  private static void decline_invite(ActorRef userActor){
    userActor.tell(new ClientInviteDeclined(), ActorRef.noSender());
  }
  //  ------------ Handeling group-related commands ------------ 
  private static void handle_group_cmd(ActorRef userActor, String input){
    String[] input_array = input.split("\\s+");  // remove spaces and seperate

    String command = input_array[1];
    if (command.equals("create") && input_array.length >= 3){
      String group_name = input_array[2];
      userActor.tell(new ClientGroupCreate(group_name), ActorRef.noSender());
    }else if(command.equals("leave") && input_array.length >= 3){
      String group_name = input_array[2];
      userActor.tell(new ClientGroupLeave(group_name), ActorRef.noSender());
    // User->Group commands
    }else if(command.equals("send")  && input_array.length >= 5){
      String send_type = input_array[2];
      String group_name = input_array[3];
      if(send_type.equals("text")){
        String text = cutText(input_array, 4);
        userActor.tell(new ClientGroupText(group_name, text), ActorRef.noSender());
      }else if(send_type.equals("file")){
        String path = input_array[4];
        byte[] file = readFile(path);
        if (file != null)
          userActor.tell(new ClientGroupFile(group_name, file), ActorRef.noSender());
      }
      //  Group related commands
    }else if(command.equals("user")  && input_array.length >= 5){
      String action = input_array[2];
      String group_name = input_array[3];
      String target_name = input_array[4];
      if      (action.equals("invite")){
        userActor.tell(new ClientGroupInvite(group_name, target_name), ActorRef.noSender());
      }else if(action.equals("remove")){
        userActor.tell(new ClientGroupUserRem(group_name, target_name), ActorRef.noSender());
      }else if(action.equals("mute")){
        int mute_time = Integer.parseInt(input_array[5]);
        userActor.tell(new ClientGroupUserMute(group_name, target_name, mute_time), ActorRef.noSender());
      }else if(action.equals("unmute")){
        userActor.tell(new ClientGroupUserUnmute(group_name, target_name), ActorRef.noSender());
      }
      // COADMIN commands
    }else if(command.equals("coadmin")  && input_array.length >= 5){
      String action = input_array[2];
      String group_name = input_array[3];
      String target_name = input_array[4];
      if(action.equals("add")){
        userActor.tell(new ClientGroupAddCoAdmin(group_name, target_name), ActorRef.noSender());
      }else if(action.equals("remove")){
        userActor.tell(new ClientGroupRemCoAdmin(group_name, target_name), ActorRef.noSender());
      }
    }else{
      p("Unknown command: '" + input + "' ,Try again or type 'exit'");
    }
  }

  // Assist functions
  private static byte[] readFile(String path){
    byte[] data = null;
    try{ data = Files.readAllBytes(Paths.get(path)); } 
    catch(IOException error){ p(path + " path does not exist!"); }
    return data;
  }

  private static void p(String s){
    System.out.println(s);
  }

  private static String cutText(String[] array, int index){
    return String.join(" ", Arrays.copyOfRange(array, index, array.length)); // TODO: maybe minus 1?
  }
}
