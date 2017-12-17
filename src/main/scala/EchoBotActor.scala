import scala.io.Source
import UserActor.UserMessage
import akka.actor.{Actor, Props}
import info.mukel.telegrambot4s.api.declarative.Commands
import info.mukel.telegrambot4s.api.{Polling, TelegramBot}
import info.mukel.telegrambot4s.models.Message

class EchoBotActor extends TelegramBot with Polling with Commands with Actor {
  // Use 'def' or 'lazy val' for the token, using a plain 'val' may/will
  // lead to initialization order issues.
  // Fetch the token from an environment variable or untracked file.
  lazy val token = scala.util.Properties
    .envOrNone("BOT_TOKEN")
    .getOrElse(Source.fromFile("bot.token").getLines().mkString)

  onCommand('hello) { implicit msg =>
    reply("My token is SAFE!")
  }

  onMessage { implicit msg =>
    msg.from.map(_.id.toString).foreach { userId =>
      val userActor = context
        .child(userId)
        .getOrElse(
          context.actorOf(UserActor.props(userId), userId)
        )
      userActor ! UserMessage(msg.text.getOrElse(":)"))
    }
  }

  override def preStart(): Unit = {
    run()
  }

  override def receive: Receive = {
    case sm @ SendMessage(UserMessage(message)) => reply(message)(sm.message)
  }
}

case class SendMessage(userMessage: UserMessage)(implicit val message: Message)

object EchoBotActor {
  def props = Props(new EchoBotActor)
}
