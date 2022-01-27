package sample

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import sample.LeaderActor.{Failed, Finish}

import scala.concurrent.Future
import scala.util.{Failure, Success}

object FollowerActor {

  sealed trait Command
  case class ExecTask(taskId: String, replyTo: ActorRef[LeaderActor.Command])
      extends Command

  sealed trait ExecResult {
    val taskId: String
  }
  case class ExecSuccess(taskId: String) extends ExecResult
  case class ExecFailure(taskId: String, reason: String) extends ExecResult
  case class WrappedExecResult(execResult: ExecResult,
                               reply: ActorRef[LeaderActor.Command])
      extends Command

  def apply(operation: FollowerOperation): Behavior[Command] =
    Behaviors.receive[Command] { (context, message) =>
      message match {
        case ExecTask(taskId, replyTo) =>
          context.log.info(s"receive ExecTask. start task taskId: ${taskId}")
          context.pipeToSelf(operation.exec(taskId)) {
            case Success(_) => WrappedExecResult(ExecSuccess(taskId), replyTo)
            case Failure(e) =>
              WrappedExecResult(ExecFailure(taskId, e.getMessage), replyTo)
          }
          Behaviors.same

        case WrappedExecResult(result, replyTo) =>
          result match {
            case ExecSuccess(taskId)         => replyTo ! Finish(taskId)
            case ExecFailure(taskId, reason) => replyTo ! Failed(taskId, reason)
          }
          Behaviors.same
      }
    }
}

trait FollowerOperation {
  def exec(taskId: String): Future[Unit]
}

class FollowerOperationImpl extends FollowerOperation {

  // ダミーの実装です
  def exec(taskId: String): Future[Unit] = Future.successful(())
}
