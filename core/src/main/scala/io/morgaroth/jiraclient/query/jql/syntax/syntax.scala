package io.morgaroth.jiraclient.query.jql.syntax

import scala.language.implicitConversions

sealed trait JqlQEntry {
  val space = """ """
  val equal = "="

  def render: String

  def isNothing = false
}

case object JqlNothing extends JqlQEntry {
  override lazy val render: String = "NOTHING"

  override def isNothing: Boolean = true
}

case class JqlEq(key: String, value: String) extends JqlQEntry {
  override lazy val render: String = s"""$key$equal"$value""""
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
