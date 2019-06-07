package whatsapp.client;
// package internal imports
import whatsapp.client.User.*;
// import whatsapp.client.User.ClientConnectMessage;
// import whatsapp.client.User.ClientDisconnectMessage;
// import whatsapp.client.User.ClientSendText;
// import whatsapp.client.User.ClientSendFile;

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

      //  ------------Main - input ------------ 
    try {
      //#create-user-actor
      final ActorRef userActor = 
        system.actorOf(User.props(), "userActor");

        // Scanner in or System.in.read?
        Scanner scanner = new Scanner(System.in);

        String input;
        while(true){
            input = scanner.nextLine();
            if(input.startsWith("/user")){
              handle_user_cmd(userActor, input);
            }else if(input.startsWith("/group")){
              handle_group_cmd(userActor, input);
            }else if(input.startsWith("exit")){
              break;
            }else{
              System.out.println("Unknown command: " + input/* + " ,Try again or type 'exit'"*/);
            }
        }

    // } catch (IOException ioe) {
    } finally {
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

    }else{ // unknown command, error? what to do in this case..
    }
  }

  //  ------------ Handeling group-related commands ------------ 
  private static void handle_group_cmd(ActorRef userActor, String input){
    String[] input_array = input.split("\\s+");  // remove spaces and seperate
    // input array structure:
    // [0] /group, [1] command, [2] first param
    String command = input_array[1];
    // handeling command
    if      (command.equals("create")){

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
        // System.out.println("[debug] received file send at user");
        // String file_path = input_array[4];
      
        // userActor.tell(new ClientGroupFile(group_name, file), ActorRef.noSender());
      }
    }else{ // unknown command, error? what to do in this case..
    }
  }

  // Assist functions
  public static byte[] readFile(String path){
    byte[] data = null;
    try{ data = Files.readAllBytes(Paths.get(path)); } 
    catch(IOException error){ System.out.println(path + " path does not exist!"); }
    return data;
  }
}





// Code graveyard R.I.P
