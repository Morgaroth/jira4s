package io.morgaroth.jiraclient.marshalling

import io.circe.generic.auto._
import io.morgaroth.jiraclient.models.JiraPaginatedIssues
import org.scalatest.Inspectors
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.io.Source

class JiraMarshallingTest extends AnyFlatSpec with Matchers with Jira4sMarshalling with Inspectors {

  behavior of "Jira4sMarshalling"

  def allInputsFrom(inputs: String*)(test: String => Unit): Unit = {
    forAll(inputs)(res => test(Source.fromResource(if (!res.endsWith(".json")) s"$res.json" else res).mkString))
  }

  it should "read correctly issues list" in {
    allInputsFrom("paginated_issues_1", "paginated_issues_2") { rawJson =>
      val result = MJson.read[JiraPaginatedIssues](rawJson)
      result shouldBe Symbol("right")
    }
  }
}
