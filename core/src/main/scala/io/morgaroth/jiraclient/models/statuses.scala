package io.morgaroth.jiraclient.models

sealed trait IssueStatus {
  def repr: String
}

object IssueStatus {
  val all: Seq[IssueStatus] = Seq(Open, Closed)
  val byName: Map[String, IssueStatus] = all.map(x => x.repr -> x).toMap

  case object Open extends IssueStatus {
    override val repr: String = "OPEN"
  }

  case object Closed extends IssueStatus {
    override val repr: String = "CLOSED"
  }

}
