package sample

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.mockito.MockitoSugar.{mock, when}
import org.scalatest.wordspec.AnyWordSpecLike
import sample.FollowerActor.ExecTask
import sample.LeaderActor.{Failed, Finish}

import scala.concurrent.Future

class FollowerActorAsynchronousSpec
    extends ScalaTestWithActorTestKit
    with AnyWordSpecLike {

  "FollowerActor" must {
    "ExecTaskを受け取り、処理が成功した場合はLeaderActorへFinishを返す。" in {
      // 非同期テストの場合は実際にActorを生成するのでActorへmockを渡すことができる。
      val mockFollowerOperation = mock[FollowerOperation]
      when(mockFollowerOperation.exec("1")).thenReturn(Future.successful(()))

      //TestProbeはクエリ可能なメールボックスで、アクターの代わりに仕様することができ、受信したメッセージはアサートにすることができます。
      val probe = testKit.createTestProbe[LeaderActor.Command]()

      // testKitからActorを生成
      val follower = testKit.spawn(FollowerActor(mockFollowerOperation))
      follower ! ExecTask("1", probe.ref)

      // TestProbeのメッセージボックスにFinishが受信できていることを確認
      probe.expectMessage(Finish("1"))
    }

    "ExecTaskを受け取り、処理が失敗した場合はLeaderActorへFailedを返す。" in {
      val mockFollowerOperation = mock[FollowerOperation]
      // ExecTask処理で失敗させるためのMockを記述
      when(mockFollowerOperation.exec("1"))
        .thenReturn(Future.failed(new Exception("IO Error")))
      val probe = testKit.createTestProbe[LeaderActor.Command]()
      val follower = testKit.spawn(FollowerActor(mockFollowerOperation))
      follower ! ExecTask("1", probe.ref)
      probe.expectMessage(Failed("1", "IO Error"))
    }
  }
}
