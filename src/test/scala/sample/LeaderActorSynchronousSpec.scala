package sample

import akka.actor.testkit.typed.CapturedLogEvent
import akka.actor.testkit.typed.Effect.TimerScheduled
import akka.actor.testkit.typed.scaladsl.{
  BehaviorTestKit,
  ScalaTestWithActorTestKit
}
import org.scalatest.wordspec.AnyWordSpecLike
import org.slf4j.event.Level
import sample.FollowerActor.ExecTask
import sample.LeaderActor.{Failed, Finish, Start}

class LeaderActorSynchronousSpec
    extends ScalaTestWithActorTestKit
    with AnyWordSpecLike {

  "LeaderActor" must {

    //　副作用の確認
    "LeaderActorは起動すると、TimerScheduledが実行される" in {
      val testKit = BehaviorTestKit(LeaderActor(Modules))
      testKit.run(Start)
      testKit.expectEffectType[TimerScheduled[Start.type]]
    }

    // メッセージの確認
    "LeaderActorにStartを渡すと、子アクターのFollowerActorへExecTaskを送る" in {
      val testKit = BehaviorTestKit(LeaderActor(Modules))
      val childInbox =
        testKit.childInbox[FollowerActor.Command]("FollowerActor")
      testKit.run(Start)
      childInbox.receiveMessage() shouldBe an[ExecTask]
    }

    // ログ出力
    "Finishを受け取ると「finish taskId」をInfoログに出力する" in {
      val testKit = BehaviorTestKit(LeaderActor(Modules))
      testKit.run(Finish("taskId1"))
      testKit.logEntries() shouldBe Seq(
        CapturedLogEvent(Level.INFO, "finish taskId taskId1")
      )
    }

    // ログ出力
    "Failedを受け取ると「failed taskId」をWarnログに出力する" in {
      val testKit = BehaviorTestKit(LeaderActor(Modules))
      testKit.run(Failed("taskId1", "IO Error"))
      testKit.logEntries() shouldBe Seq(
        CapturedLogEvent(Level.WARN, "failed taskId taskId1. IO Error")
      )
    }
  }
}
