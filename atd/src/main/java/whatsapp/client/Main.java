package whatsapp.client;
// package internal imports
import whatsapp.common.ConnectMessage;


// Akka imports
import akka.actor.ActorRef;
import akka.actor.ActorSystem;

import com.typesafe.config.ConfigFactory;
// Java imports
import java.util.Scanner;
// import java.io.IOException;


public class Main {
  public static void main(String[] args) {
    final ActorSystem system = ActorSystem.create("client", ConfigFactory.load("client"));
    try {
      //#create-actors
      final ActorRef userActor = 
        system.actorOf(User.props(), "userActor");

        // Scanner in or System.in.read?
        Scanner scanner = new Scanner(System.in);

        String input;
        while(true){
            System.out.print(">");
            input = scanner.nextLine();
            if(input.startsWith("/user")){
              handle_user_cmd(userActor, input);
            }
            // TODO: handle /group
        }

    // } catch (IOException ioe) { // TODO: maybe i need to handle this?
    } finally {
      system.terminate();
    }
  }

  private static void handle_user_cmd(ActorRef userActor, String input){
    System.out.println("debug: [handle_user_cmd] input=" + input);
    String[] input_array = input.split("\\s+");  // remove spaces and seperate
    // input array structure:
    // [0] /user, [1] command, [2] first param
    String command = input_array[1];
    String sec_param = input_array[2];
    System.out.println("debug: [handle_user_cmd] command=" + command + " sec_param="+sec_param);
    // handeling command
    if      (command.equals("connect")){
    System.out.println("debug: [handle_user_cmd] entered connect command case");

      userActor.tell(new ConnectMessage(sec_param, ActorRef.noSender()), ActorRef.noSender()); // TODO: is it okay for connect message to receive nosender? maybe send getself?
    }else if(command.equals("disconnect")){
      userActor.tell(new disconnectMessage(), ActorRef.noSender());
    // USER COMMUNICATION
    }else if(command.equals("text")){
    }else if(command.equals("file")){
    }else{ // unknown command, error? what to do in this case..
    }
  }

  // Behaviours:
  public static class disconnectMessage{}
}






// Code graveyard R.I.P
  /*
  public static class connectMessage{ // Moved to common folder, delete if won't be neccesery.
    String username;
    public connectMessage(String username){
      this.username = username;
    }
  }*/