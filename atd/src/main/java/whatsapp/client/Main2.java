package whatsapp.client;

import java.io.IOException;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
// import akka.actor.ActorSelection;

import whatsapp.client.User.Connect;

public class Main {
  public static void main(String[] args) {
    final ActorSystem system = ActorSystem.create("userSystem");
    try {
      //#create-actors
      final ActorRef userActor = 
        system.actorOf(User.props(), "userActor");

      // ActorSelection selection =
        // context.actorSelection("akka.tcp://app@10.0.0.1:2552/user/serviceA/worker");
      // selection.tell("Pretty awesome feature", getSelf());

      //#main-send-messages
      userActor.tell(new Connect(), ActorRef.noSender());

      System.out.println(">>> Terminal: <<<");
      System.in.read();

    } catch (IOException ioe) {
    } finally {
      system.terminate();
    }
  }
}