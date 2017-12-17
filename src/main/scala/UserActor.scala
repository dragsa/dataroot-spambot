import UserActor.{SendNow, UserMessage}
import akka.actor.{Actor, Cancellable, Props}
import akka.event.Logging
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import info.mukel.telegrambot4s.models.Message

class UserActor(userId: String) extends Actor {
  val log = Logging(context.system, this)

  var currentMessage: Option[UserMessage] = None
  var currentSendNowCancellable: Option[Cancellable] = None

  override def receive: Receive = {
    case um @ UserMessage(msg) => {
      log.info(s"received message: $msg")
      currentMessage = Option(um)
      currentSendNowCancellable.map(_.cancel)
      currentSendNowCancellable = Option(
        context.system.scheduler.schedule(2.second, 1.second, self, SendNow))
    }
    case SendNow =>
      currentMessage.foreach { message =>
        context.parent ! SendMessage(message)(message.message)
      }
    case _ => log.info("received unknown message")
  }
}

object UserActor {
  case class UserMessage(value: String)(implicit val message: Message)
  case object SendNow

  def props(userId: String) = Props(new UserActor(userId))
}
