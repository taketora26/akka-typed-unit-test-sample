package sample

import akka.actor.typed.ActorSystem

object App {
  def main(args: Array[String]): Unit = {
    ActorSystem[LeaderActor.Command](LeaderActor(Modules), "LeaderActor")
  }
}
