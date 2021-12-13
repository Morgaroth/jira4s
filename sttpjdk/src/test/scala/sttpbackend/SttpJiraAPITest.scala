package io.gitlab.mateuszjaje.jiraclient
package sttpbackend

import createmodels.{CreateJiraIssue, JiraIssueId, JiraProjectId, PriorityId}
import models.IssueKey

import org.scalatest.DoNotDiscover
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.ExecutionContext.Implicits.global

@DoNotDiscover
class SttpJiraAPITest extends AnyFlatSpec with Matchers with HelperClasses {

  private val jiraAddress = Option(System.getenv("JIRA_ADDRESS"))
  private val jiraLogin   = Option(System.getenv("JIRA_USERNAME"))
  private val jiraToken   = Option(System.getenv("JIRA_PASSWORD"))
  assume(jiraAddress.isDefined, "jira-address env must be set for this test")
  assume(jiraLogin.isDefined, "jira-login env must be set for this test")
  assume(jiraToken.isDefined, "jira-access-token env must be set for this test")

  private val cfg = JiraConfig(jiraAddress.get, jiraLogin.get, jiraToken.get)
  val client      = new SttpJiraAPISync(cfg, JiraRestAPIConfig(true))

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
    val response = client.createIssue(payload).value.rightValue
    //    val moved = client.moveIssuesToEpic(IssueKey(""), Vector(IssueKey(response.id)))
    println(response)
  }

  it should "get ticket" in {
    val ticket = client.getIssue(IssueKey("MDT2-1444")).value.rightValue
    println(ticket)
  }

}
