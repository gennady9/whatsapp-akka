package whatsapp.client;
// package internal imports
import whatsapp.client.User.ClientConnectMessage;
import whatsapp.client.User.ClientDisconnectMessage;


// Akka imports
import akka.actor.ActorRef;
import akka.actor.ActorSystem;

import com.typesafe.config.ConfigFactory;
// Java imports
import java.io.IOException;
import java.util.Scanner;


public class Main {
  public static void main(String[] args) {
    final ActorSystem system = ActorSystem.create("client", ConfigFactory.load("client"));
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
            }
            // TODO: handle /group
        }

    // } catch (IOException ioe) {
    } finally {
      system.terminate();
    }
  }

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
    }else if(command.equals("file")){
    }else{ // unknown command, error? what to do in this case..
    }
  }
}






// Code graveyard R.I.P
