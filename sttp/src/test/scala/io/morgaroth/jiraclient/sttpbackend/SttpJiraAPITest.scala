package io.morgaroth.jiraclient.sttpbackend

import io.morgaroth.jiraclient._
import io.morgaroth.jiraclient.createmodels.{CreateJiraIssue, JiraIssueId, JiraProjectId, PriorityId}
import io.morgaroth.jiraclient.models.IssueKey
import org.scalatest.DoNotDiscover
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.{Minutes, Span}

import scala.concurrent.ExecutionContext.Implicits.global

@DoNotDiscover
class SttpJiraAPITest extends AnyFlatSpec with Matchers with ScalaFutures with HelperClasses {

  implicit override def patienceConfig: PatienceConfig = PatienceConfig(Span(1, Minutes))

  private val jiraAddress = Option(System.getenv("JIRA_ADDRESS"))
  private val jiraLogin   = Option(System.getenv("JIRA_USERNAME"))
  private val jiraToken   = Option(System.getenv("JIRA_PASSWORD"))
  assume(jiraAddress.isDefined, "jira-address env must be set for this test")
  assume(jiraLogin.isDefined, "jira-login env must be set for this test")
  assume(jiraToken.isDefined, "jira-access-token env must be set for this test")

  private val cfg = JiraConfig(jiraAddress.get, jiraLogin.get, jiraToken.get)
  val client      = new SttpJiraAPI(cfg, JiraRestAPIConfig(true))

  behavior of "SttpJiraAPI"

  it should "create ticket" in {
    val projectKey = JiraProjectId.key("MDT2")
    val payload = CreateJiraIssue(
      projectKey,
      "Some summary 2",
      "description?",
      JiraIssueId.bug,
      Some(Set("label-1", "label-2")),
      Some(PriorityId.Critical),
      Map("customfield_12120" -> "MDT2-1"),
    )
    val response = client.createIssue(payload).value.futureValue.rightValue
    //    val moved = client.moveIssuesToEpic(IssueKey(""), Vector(IssueKey(response.id)))
    println(response)
  }

  it should "get ticket" in {
    val ticket = client.getIssue(IssueKey("MDT2-1444")).value.futureValue.rightValue
    println(ticket)
  }

}
