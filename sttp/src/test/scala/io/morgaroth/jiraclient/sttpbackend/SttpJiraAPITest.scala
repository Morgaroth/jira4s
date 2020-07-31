package io.morgaroth.jiraclient.sttpbackend

import io.morgaroth.jiraclient._
import io.morgaroth.jiraclient.createmodels.{CreateJiraIssue, JiraIssueId, JiraProjectId, PriorityId}
import org.scalatest.DoNotDiscover
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.{Minutes, Span}

import scala.concurrent.ExecutionContext.Implicits.global

@DoNotDiscover
class SttpJiraAPITest extends AnyFlatSpec with Matchers with ScalaFutures with HelperClasses {

  override implicit def patienceConfig: PatienceConfig = PatienceConfig(Span(1, Minutes))

  private val jiraAddress = Option(System.getenv("jira-address"))
  private val jiraLogin = Option(System.getenv("jira-login"))
  private val jiraToken = Option(System.getenv("jira-access-token"))
  assume(jiraAddress.isDefined, "jira-address env must be set for this test")
  assume(jiraLogin.isDefined, "jira-login env must be set for this test")
  assume(jiraToken.isDefined, "jira-access-token env must be set for this test")

  private val cfg = JiraConfig(jiraAddress.get, jiraLogin.get, jiraToken.get)
  val client = new SttpJiraAPI(cfg, JiraRestAPIConfig(true))

  behavior of "SttpJiraAPI"

  it should "create ticket" in {
    val projectKey = JiraProjectId.key("")
    val payload = CreateJiraIssue(
      projectKey,
      "Some summary",
      "description?",
      JiraIssueId.bug,
      Some(Set("label-1", "label-2")),
      Some(PriorityId.Critical),
    )
    val response = client.createIssue(payload).value.futureValue.rightValue
    //    val moved = client.moveIssuesToEpic(IssueKey(""), Vector(IssueKey(response.id)))
    println(response)
  }

}
