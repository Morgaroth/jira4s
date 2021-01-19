package io.morgaroth.jiraclient.models

import io.circe.Codec
import io.morgaroth.jiraclient.marshalling.{EnumMarshalling, EnumMarshallingGlue}

trait JiraEnum {
  def name: String
}

sealed trait IssueStatus extends JiraEnum

object IssueStatus extends EnumMarshallingGlue[IssueStatus] {
  val all    = Seq(Open, Closed)
  val byName = all.map(x => x.name -> x).toMap

  case object Open extends IssueStatus {
    override val name: String = "OPEN"
  }

  case object Closed extends IssueStatus {
    override val name: String = "CLOSED"
  }

  override def rawValue = _.name

  implicit val IssueStatusCirceCodec: Codec[IssueStatus] = EnumMarshalling.stringEnumCodecOf(IssueStatus)
}

sealed trait Resolution extends JiraEnum

object Resolution extends EnumMarshallingGlue[Resolution] {
  val all: Seq[Resolution] = Seq(
    Complete,
    Invalid,
    ClosedToArchive,
    Rejected,
    Done,
    ReadyForTesting,
    DeScopeFromSprint,
    PassedTesting,
    Released,
    Fixed,
    Resolved,
    WontDo,
    CannotReproduce,
    Duplicate,
    Deferred,
    WontFix,
  )

  val byName: Map[String, Resolution] = all.map(x => x.name -> x).toMap

  abstract class ResolutionBase(val name: String) extends Resolution

  case object Complete extends ResolutionBase("Complete")

  case object Invalid extends ResolutionBase("Invalid")

  case object ClosedToArchive extends ResolutionBase("Closed to Archive")

  case object Rejected extends ResolutionBase("Rejected")

  case object Done extends ResolutionBase("Done")

  case object ReadyForTesting extends ResolutionBase("Ready for Testing")

  case object DeScopeFromSprint extends ResolutionBase("De-scope from Sprint")

  case object PassedTesting extends ResolutionBase("Passed Testing")

  case object Released extends ResolutionBase("Released")

  case object Fixed extends ResolutionBase("Fixed")

  case object Resolved extends ResolutionBase("Resolved")

  case object WontDo extends ResolutionBase("Won't Do")

  case object CannotReproduce extends ResolutionBase("Cannot Reproduce")

  case object Duplicate extends ResolutionBase("Duplicate")

  case object Deferred extends ResolutionBase("Deferred")

  case object WontFix extends ResolutionBase("Won't Fix")

  // special one, it is a set of statuses, usable only for querying
  case object Unresolved extends ResolutionBase("unresolved")

  override def rawValue = _.name

  implicit val ResolutionCirceCodec: Codec[Resolution] = EnumMarshalling.stringEnumCodecOf(Resolution)

}
