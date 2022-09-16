package io.gitlab.mateuszjaje.jiraclient
package marshalling

import models.JiraPaginatedIssues

import cats.syntax.either.*
import org.scalatest.Inspectors
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.io.Source

class JiraMarshallingTest extends AnyFlatSpec with Matchers with Jira4sMarshalling with Inspectors {

  behavior of "Jira4sMarshalling"

  def allInputsFrom(inputs: String*)(test: String => Unit): Unit = {
    forAll(inputs) { res =>
      val string = loadResource(res)
      test(string)
    }
  }

  private def loadResource(res: String) =
    Source.fromResource(if (!res.endsWith(".json")) s"$res.json" else res).mkString

  it should "read correctly issues list" in {
    MJson.read[JiraPaginatedIssues](loadResource("paginated_issues_1.json")) shouldBe Symbol("right")
    val res = MJson.read[JiraPaginatedIssues](loadResource("paginated_issues_2.json"))
    res shouldBe Symbol("right")
    val issuesWithCustomFields = res.valueOr(throw _).values.filter(_.fields.customFields.nonEmpty)
    issuesWithCustomFields should not be empty
    issuesWithCustomFields.head.fields.customFields should not be empty
  }
}
