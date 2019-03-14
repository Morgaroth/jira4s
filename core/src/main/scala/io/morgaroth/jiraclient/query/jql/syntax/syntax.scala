package io.morgaroth.jiraclient.query.jql.syntax

import scala.language.implicitConversions


sealed trait JqlQEntry {
  val space = "%20"
  val equal = "%3D"

  def render: String

  def isNothing = false
}

object JqlQEntry {

  implicit class HigherSyntax(left: JqlQEntry) {
    def and(right: JqlQEntry): JqlQEntry =
      if (left.isNothing) right else right match {
        case JqlNothing => left
        case _ => JqlAnd(left, right)
      }

    def or(right: JqlQEntry): JqlQEntry = JqlOr(left, right)

    def orderBy(right: JqlOrdering) = JqlOrderedQuery(left, right)
  }

  implicit class LowerSyntax(left: String) {
    def desc: JqlOrdering = JqlOrdering(left, "DESC")

    def asc: JqlOrdering = JqlOrdering(left, "ASC")

    def gte(right: String): JqlQEntry = JqlGte(left, right)

    def lte(right: String): JqlQEntry = JqlLte(left, right)

    def gt(right: String): JqlQEntry = JqlGt(left, right)

    def lt(right: String): JqlQEntry = JqlLt(left, right)

    def gte(right: Long): JqlQEntry = JqlGte(left, right.toString)

    def lte(right: Long): JqlQEntry = JqlLte(left, right.toString)

    def gt(right: Long): JqlQEntry = JqlGt(left, right.toString)

    def lt(right: Long): JqlQEntry = JqlLt(left, right.toString)

    def ~(right: String): JqlQEntry = JqlLike(left, right)

    def ===(right: String): JqlQEntry = JqlEq(left, right)

    def ===(right: Int): JqlQEntry = JqlEq(left, right.toString)

    def ===(right: Long): JqlQEntry = JqlEq(left, right.toString)

    def !==(right: String): JqlQEntry = JqlNEq(left, right)

    def !==(right: Int): JqlQEntry = JqlNEq(left, right.toString)

    def !==(right: Long): JqlQEntry = JqlNEq(left, right.toString)

    def in(right: JqlSet): JqlQEntry = JqlIn(left, right)

    def <&(right: JqlSet): JqlQEntry = JqlIn(left, right)

    def notIn(right: JqlSet): JqlQEntry = if (right.elements.isEmpty) JqlNothing else JqlNotIn(left, right)

    def !<&(right: JqlSet): JqlQEntry = JqlNotIn(left, right)
  }

  implicit def wrapSetToJql(raw: Set[String]): JqlSet = JqlSet(raw)

}

case object JqlNothing extends JqlQEntry {
  override lazy val render: String = "NOTHING"

  override def isNothing: Boolean = true
}

case class JqlEq(key: String, value: String) extends JqlQEntry {
  override lazy val render: String = s"""$key$space$equal$space"$value""""
}

abstract class JqlOrd(key: String, value: String, op: String) extends JqlQEntry {
  override lazy val render: String = s"$key$space$op$space$value"
}

case class JqlGt(key: String, value: String) extends JqlOrd(key, value, ">")

case class JqlLt(key: String, value: String) extends JqlOrd(key, value, "<")

case class JqlGte(key: String, value: String) extends JqlOrd(key, value, ">=")

case class JqlLte(key: String, value: String) extends JqlOrd(key, value, "<=")

case class JqlNEq(key: String, value: String) extends JqlQEntry {
  override lazy val render: String = s"$key$space!$equal$space$value"

}

case class JqlLike(key: String, value: String) extends JqlQEntry {
  override lazy val render: String = s"""$key$space~$space"$value""""

}

case class JqlSet(elements: Set[String]) extends JqlQEntry {
  override lazy val render: String = elements.mkString(s"($space", s",$space", ")")
}

abstract class JqlLogicOperator(j1: JqlQEntry, o: String, j2: JqlQEntry) extends JqlQEntry {
  override lazy val render: String = {
    val op = o.replace(" ", space)
    s"${j1.render}$space$op$space${j2.render}"
  }
}


abstract class JqlSetOperator(j1: String, o: String, j2: JqlQEntry) extends JqlQEntry {
  override lazy val render: String = {
    val op = o.replace(" ", space)
    s"$j1$space$op$space${j2.render}"
  }
}

case class JqlOr(j1: JqlQEntry, j2: JqlQEntry) extends JqlLogicOperator(j1, "OR", j2)

case class JqlAnd(j1: JqlQEntry, j2: JqlQEntry) extends JqlLogicOperator(j1, "AND", j2)

case class JqlIn(j1: String, j2: JqlSet) extends JqlSetOperator(j1, "IN", j2)

case class JqlNotIn(j1: String, j2: JqlSet) extends JqlSetOperator(j1, "NOT IN", j2)

case class JqlOrdering(value: String, direction: String)

case class JqlOrderedQuery(lrs: JqlQEntry, o: JqlOrdering) extends JqlQEntry {
  override def render: String = s"${lrs.render}${space}ORDER${space}BY$space${o.value}$space${o.direction}"
}
