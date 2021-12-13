package io.gitlab.mateuszjaje.jiraclient
package query
package jql

import models.JiraEnum

import scala.language.implicitConversions

package object syntax {

  implicit class HigherSyntax(left: JqlQEntry) {
    def and(right: JqlQEntry): JqlQEntry =
      if (left.isNothing) right
      else
        right match {
          case JqlNothing => left
          case _          => JqlAnd(left, right)
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

    def ===(right: JiraEnum): JqlQEntry = JqlEq(left, right.name)

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

  implicit def wrapSetToJql(raw: Set[JiraEnum])(implicit du: DummyImplicit): JqlSet = JqlSet(raw.map(_.name))

}
