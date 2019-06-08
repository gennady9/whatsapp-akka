package whatsapp.client;
// package internal imports
import whatsapp.client.ClientMessages.*;
// Akka imports
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.typesafe.config.ConfigFactory;
// Java imports
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
              System.out.println("Unknown command: '" + input + "' ,Try again or type 'exit'");
            }
        }
    // } catch (IOException ioe) {
    } finally {
      scanner.close();
      system.terminate();
    }
  }

    //  ------------ Handeling user-related commands ------------ 
  private static void handle_user_cmd(ActorRef userActor, String input){
    String[] input_array = input.split("\\s+");  // remove spaces and seperate
    // input array structure:
    // [0] /user, [1] command, [2] first param
    String command = input_array[1];
    // handeling command
    if      (command.equals("connect")){

      String param = input_array[2];
      userActor.tell(new ClientConnectMessage(param), ActorRef.noSender());

    }else if(command.equals("disconnect")){

      userActor.tell(new ClientDisconnectMessage(), ActorRef.noSender());

    // USER COMMUNICATION
    }else if(command.equals("text")){

      String target_name = input_array[2];
      String text = input_array[3];
      userActor.tell(new ClientSendText(target_name, text), ActorRef.noSender());

    }else if(command.equals("file")){

      String target_name = input_array[2];
      String path = input_array[3];
      byte[] file = readFile(path);
      if (file != null)
        userActor.tell(new ClientSendFile(target_name, file), ActorRef.noSender());

    }else{ // TODO: unknown command, error? what to do in this case..
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
    // input array structure:
    // [0] /group, [1] command, [2] first param
    String command = input_array[1];
    // String command = safeParamGet(input_array, 1);
    // if((command = safeParamGet(input_array, 1)) == "invalid")
    // handeling command
    if      (command.equals("create")){

      if(input_array.length < 3) return;
      String group_name = input_array[2];
      
      userActor.tell(new ClientGroupCreate(group_name), ActorRef.noSender());

    }else if(command.equals("leave")){

      String group_name = input_array[2];
      userActor.tell(new ClientGroupLeave(group_name), ActorRef.noSender());

    // GROUP COMMUNICATION
    }else if(command.equals("send")){

      String send_type = input_array[2];
      String group_name = input_array[3];
      if(send_type.equals("text")){
        String text = input_array[4];
        userActor.tell(new ClientGroupText(group_name, text), ActorRef.noSender());
      }else if(send_type.equals("file")){
        String path = input_array[4];
        byte[] file = readFile(path);
        if (file != null)
          userActor.tell(new ClientGroupFile(group_name, file), ActorRef.noSender());
      }


    }else if(command.equals("user")){
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

    }else if(command.equals("coadmin")){
      if(input_array.length < 5) return;
      String action = input_array[2];
      String group_name = input_array[3];
      String target_name = input_array[4];
      if      (action.equals("add")){
        userActor.tell(new ClientGroupAddCoAdmin(group_name, target_name), ActorRef.noSender());
      }else if(action.equals("remove")){
        userActor.tell(new ClientGroupRemCoAdmin(group_name, target_name), ActorRef.noSender());
      }
  
    
    
    }else{
      System.out.println("Unknown command: '" + input + "' ,Try again or type 'exit'");
    }
  }

  // Assist functions
  private static byte[] readFile(String path){
    byte[] data = null;
    try{ data = Files.readAllBytes(Paths.get(path)); } 
    catch(IOException error){ System.out.println(path + " path does not exist!"); }
    return data;
  }

  private static String safeParamGet(String[] array, int index){
    if(index > array.length - 1)
      return array[0];
    else
      return array[index];
  }
}







// Code graveyard R.I.P
