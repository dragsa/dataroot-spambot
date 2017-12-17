import akka.actor.ActorSystem

object Main extends App {
  val actorSystem = ActorSystem("echo-bot-actor")
  actorSystem.actorOf(EchoBotActor.props)
}
