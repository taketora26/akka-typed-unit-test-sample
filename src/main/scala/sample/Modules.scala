package sample

trait Modules {
  val followerOperation: FollowerOperation
  val leaderOperation: LeaderOperation
}

import com.softwaremill.macwire.wire

object Modules extends Modules {

  lazy val followerOperation: FollowerOperation = wire[FollowerOperationImpl]
  lazy val leaderOperation: LeaderOperation = wire[LeaderOperationImpl]

}
