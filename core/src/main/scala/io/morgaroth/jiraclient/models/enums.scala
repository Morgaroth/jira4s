package io.morgaroth.jiraclient.models

trait JiraEnum {
  def repr: String
}

sealed trait IssueStatus extends JiraEnum

object IssueStatus {
  val all: Seq[IssueStatus] = Seq(Open, Closed)
  val byName: Map[String, IssueStatus] = all.map(x => x.repr -> x).toMap

  def fromRepr(repr: String): IssueStatus = byName.getOrElse(repr, Unknown(repr))


  case object Open extends IssueStatus {
    override val repr: String = "OPEN"
  }

  case object Closed extends IssueStatus {
    override val repr: String = "CLOSED"
  }

  case class Unknown(repr: String) extends IssueStatus

}


sealed trait Resolution extends JiraEnum

object Resolution {
  val all: Seq[Resolution] = Seq(Unresolved)
  val byName: Map[String, Resolution] = all.map(x => x.repr -> x).toMap

  def fromRepr(repr: String): Resolution = byName.getOrElse(repr, Unknown(repr))

  case object Unresolved extends Resolution {
    override def repr: String = "unresolved"
  }

  case class Unknown(repr: String) extends Resolution

}
