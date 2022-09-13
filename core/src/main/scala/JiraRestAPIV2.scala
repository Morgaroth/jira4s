package io.gitlab.mateuszjaje.jiraclient

import apisv2.{CreateIssueAPIV2, JiraApiT, UpdateIssueAPIV2}
import marshalling.Jira4sMarshalling
import models.*
import query.jql.syntax.*
import query.syntax.*

import JiraApiT.syntax.*

trait JiraRestAPIV2[F[_]] extends Jira4sMarshalling with UpdateIssueAPIV2[F] with CreateIssueAPIV2[F] {

  val API  = "rest/api/2"
  val API1 = "rest/agile/1.0"

  implicit def m: JiraApiT[F]

  def config: JiraConfig

  protected val reqGen = RequestGenerator(config)

  protected def invokeRequest(request: JiraRequest)(implicit requestId: RequestId): F[Either[JiraError, String]]

  /** GET /rest/api/2/project/search
    *
    * @see https://developer.atlassian.com/cloud/jira/platform/rest/v2/#api-rest-api-2-project-search-get
    */
  def searchProjects: F[Either[JiraError, Vector[JiraProject]]] = {
    implicit val rId: RequestId = RequestId.newOne("search-projects")
    val req                     = reqGen.get(API + "/projects/search")
    invokeRequest(req).unmarshall[Vector[JiraProject]]
  }

  /** GET /rest/api/2/issue/{issueIdOrKey}
    *
    * @see https://developer.atlassian.com/cloud/jira/platform/rest/v2/#api-rest-api-2-issue-issueIdOrKey-get
    */
  def getIssue(key: IssueKey) = {
    implicit val rId: RequestId = RequestId.newOne("get-issue-by-key")
    val req                     = reqGen.get(API + s"/issue/$key")
    invokeRequest(req).unmarshall[JiraIssue]
  }

  /** GET /rest/api/2/issue/{issueIdOrKey}
    *
    * @see https://developer.atlassian.com/cloud/jira/platform/rest/v2/#api-rest-api-2-search-get
    * @see https://confluence.atlassian.com/jirasoftwarecloud/advanced-searching-764478330.html
    */
  def searchIssues(
      query: JqlQEntry,
  ) = {

    def getPage(start: Int = 0) = {
      implicit val rId: RequestId = RequestId.newOne(s"search-issues-start-at-$start")
      val req                     = reqGen.get(API + "/search", Jql(query), JPage(start, 50))
      invokeRequest(req).unmarshall[JiraPaginatedIssues]
    }

    def getAll(startFrom: Int, acc: Vector[JiraIssue]): F[Either[JiraError, Vector[JiraIssue]]] = {
      getPage(startFrom).flatMap { resp =>
        val currentIssues = acc ++ resp.issues
        val currentCnt    = currentIssues.size
        if (currentCnt >= resp.total || resp.issues.isEmpty) {
          currentIssues.pure
        } else {
          getAll(startFrom + 1, currentIssues)
        }
      }
    }

    getAll(0, Vector.empty)
  }

  /** GET /rest/api/2/issue/{issueIdOrKey}/remotelink
    *
    * @see https://developer.atlassian.com/cloud/jira/platform/rest/v2/#api-api-2-issue-issueIdOrKey-remotelink-get
    */
  def getIssueRemoteLinks(key: IssueKey) = {
    implicit val rId: RequestId = RequestId.newOne("get-issue-remote-links")
    val req                     = reqGen.get(API + s"/issue/$key/remotelink")
    invokeRequest(req).unmarshall[Vector[JiraRemoteLink]]
  }

  /** POST /rest/api/2/issue/{issueIdOrKey}/remotelink
    *
    * @see https://developer.atlassian.com/cloud/jira/platform/rest/v2/#api-api-2-issue-issueIdOrKey-remotelink-post
    */
  def createOrUpdateIssueLink(
      issueKey: IssueKey,
      globalId: String,
      link: String,
      title: String,
      resolved: Boolean,
      icon: Option[Icon] = None,
      relationship: Option[Relationship] = None,
  ) = {
    implicit val rId: RequestId = RequestId.newOne("create-or-update-issue-link")
    val payload = CreateJiraRemoteLink(
      globalId,
      None,
      relationship.map(_.raw),
      RemoteLinkObject(link, title, None, icon, Some(JiraRemoteLinkStatus(Some(resolved), None))),
    )
    val req = reqGen.post(API + s"/issue/$issueKey/remotelink", MJson.write(payload))

    invokeRequest(req).unmarshall[RemoteIssueLinkIdentifies]
  }

  /** DELETE /rest/api/2/issue/{issueIdOrKey}/remotelink/{linkid}
    *
    * @see https://developer.atlassian.com/cloud/jira/platform/rest/v2/#api-rest-api-2-issue-issueIdOrKey-remotelink-linkId-delete
    */
  def deleteRemoteLinkById(issueKey: IssueKey, linkId: Int) = {
    implicit val rId: RequestId = RequestId.newOne("delete-remote-link-by-linkid")
    val req                     = reqGen.delete(API + s"/issue/$issueKey/remotelink/$linkId")
    invokeRequest(req).map(_ => ())
  }

  /** DELETE /rest/api/2/issue/{issueIdOrKey}/remotelink
    *
    * @see https://developer.atlassian.com/cloud/jira/platform/rest/v2/#api-rest-api-2-issue-issueIdOrKey-remotelink-delete
    */
  def deleteRemoteLinkByGlobalId(issueKey: IssueKey, globalId: String) = {
    implicit val rId: RequestId = RequestId.newOne("delete-remote-link-by-globalid")
    val req                     = reqGen.delete(API + s"issue/$issueKey/remotelink", KVParam("globalId", globalId))
    invokeRequest(req).map(_ => ())
  }
}
