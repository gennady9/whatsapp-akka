package whatsapp.server;

import java.io.IOException;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

public class Main {
  public static void main(String[] args) {
    final ActorSystem system = ActorSystem.create("hello server");
    try {
      //#create-actors
      final ActorRef serverActor = 
        system.actorOf(Server.props(), "serverActor");
      //#create-actors

      //#main-send-messages

      //#main-send-messages

      System.out.println(">>> Press ENTER to exit <<<");
      System.in.read();
    } catch (IOException ioe) {
    } finally {
      system.terminate();
    }
  }
}
