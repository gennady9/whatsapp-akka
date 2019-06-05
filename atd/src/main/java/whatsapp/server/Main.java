package whatsapp.server;

import java.io.IOException;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.typesafe.config.ConfigFactory;
import whatsapp.server.ManagingServer;

public class Main {
  public static void main(String[] args) {
    final ActorSystem system = ActorSystem.create("whatsapp", ConfigFactory.load("server"));
    try {
      final ActorRef managingServerActor = system.actorOf(ManagingServer.props(), "managingServer");

      System.out.println(">>> Press ENTER to exit <<<");
      System.in.read();
      } catch (IOException ioe) {
          System.out.println(ioe.getMessage());
      } finally {
          system.terminate();
      }
  }
}
