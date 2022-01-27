package sample

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import sample.FollowerActor.ExecTask

import scala.concurrent.duration.DurationInt

object LeaderActor {

  sealed trait Command
  case object Start extends Command
  case class Finish(taskId: String) extends Command
  case class Failed(taskId: String, reason: String) extends Command

  sealed trait Status
  case object Running extends Status
  case object Waiting extends Status

  def apply(modules: Modules): Behavior[Command] = Behaviors.setup { context =>
    Behaviors.withTimers { timers =>
      timers.startTimerWithFixedDelay(Start, Start, 2.seconds)
      val follower =
        context.spawn(
          FollowerActor.apply(modules.followerOperation),
          "FollowerActor"
        )

      def behavior(status: Status): Behaviors.Receive[LeaderActor.Command] = {
        Behaviors.receiveMessage[LeaderActor.Command] {
          case Start if status == Running =>
            context.log.info("receive start, but already running")
            Behaviors.same

          case Start if status == Waiting =>
            context.log.info("start")
            val taskId = modules.leaderOperation.createTaskId
            follower ! ExecTask(taskId, context.self)
            behavior(Running)

          case Finish(taskId) =>
            context.log.info(s"finish taskId ${taskId}")
            behavior(Waiting)

          case Failed(taskId, reason) =>
            context.log.warn(s"failed taskId ${taskId}. ${reason}")
            behavior(Waiting)
        }
      }
      behavior(Waiting)
    }
  }
}

trait LeaderOperation {
  def createTaskId: String
}

class LeaderOperationImpl extends LeaderOperation {
  def createTaskId: String = java.util.UUID.randomUUID.toString
}
