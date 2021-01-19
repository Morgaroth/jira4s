package io.morgaroth.jiraclient

import cats.Monad
import cats.data.EitherT
import cats.implicits._
import io.circe.generic.auto._
import io.morgaroth.jiraclient.marshalling.Jira4sMarshalling
import io.morgaroth.jiraclient.models._
import io.morgaroth.jiraclient.query.jql.syntax._
import io.morgaroth.jiraclient.query.syntax._

import scala.language.{higherKinds, postfixOps}

trait JiraRestAPI[F[_]] extends Jira4sMarshalling with UpdateIssueAPI[F] with CreateIssueAPI[F] {

  val API  = "rest/api/2"
  val API1 = "rest/agile/1.0"

  implicit def m: Monad[F]

  def config: JiraConfig

  protected val reqGen = RequestGenerator(config)

  protected def invokeRequest(request: JiraRequest)(implicit requestId: RequestId): EitherT[F, JiraError, String]

  type JiraRespT[A] = EitherT[F, JiraError, A]

  /** GET /rest/api/2/project/search
    *
    * @see https://developer.atlassian.com/cloud/jira/platform/rest/v2/#api-rest-api-2-project-search-get
    */
  def searchProjects: EitherT[F, JiraError, Vector[JiraProject]] = {
    implicit val rId: RequestId = RequestId.newOne("search-projects")
    val req                     = reqGen.get(API + "/projects/search")
    invokeRequest(req).unmarshall[Vector[JiraProject]]
  }

  /** GET /rest/api/2/issue/{issueIdOrKey}
    *
    * @see https://developer.atlassian.com/cloud/jira/platform/rest/v2/#api-rest-api-2-issue-issueIdOrKey-get
    */
  def getIssue(key: IssueKey): JiraRespT[JiraIssue] = {
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
  ): EitherT[F, JiraError, Vector[JiraIssue]] = {

    def getPage(start: Int = 0): EitherT[F, JiraError, JiraPaginatedIssues] = {
      implicit val rId: RequestId = RequestId.newOne(s"search-issues-start-at-$start")
      val req                     = reqGen.get(API + "/search", Jql(query), JPage(start, 50))
      invokeRequest(req).unmarshall[JiraPaginatedIssues]
    }

    def getAll(startFrom: Int, acc: Vector[JiraIssue]): JiraRespT[Vector[JiraIssue]] = {
      getPage(startFrom).flatMap { resp =>
        val currentIssues = acc ++ resp.issues
        val currentCnt    = currentIssues.size
        if (currentCnt >= resp.total || resp.issues.isEmpty) {
          currentIssues.pure[JiraRespT]
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
  def getIssueRemoteLinks(key: IssueKey): JiraRespT[Vector[JiraRemoteLink]] = {
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
  ): JiraRespT[RemoteIssueLinkIdentifies] = {
    implicit val rId: RequestId = RequestId.newOne("create-or-update-issue-link")
    val payload = CreateJiraRemoteLink(
      globalId,
      None,
      relationship.map(_.raw),
      RemoteLinkObject(link, title, None, icon, JiraRemoteLinkStatus(resolved.some, None).some),
    )
    val req = reqGen.post(API + s"/issue/$issueKey/remotelink", MJson.write(payload))

    invokeRequest(req).unmarshall[RemoteIssueLinkIdentifies]
  }

  /** DELETE /rest/api/2/issue/{issueIdOrKey}/remotelink/{linkid}
    *
    * @see https://developer.atlassian.com/cloud/jira/platform/rest/v2/#api-rest-api-2-issue-issueIdOrKey-remotelink-linkId-delete
    */
  def deleteRemoteLinkById(issueKey: IssueKey, linkId: Int): JiraRespT[Unit] = {
    implicit val rId: RequestId = RequestId.newOne("delete-remote-link-by-linkid")
    val req                     = reqGen.delete(API + s"/issue/$issueKey/remotelink/$linkId")
    invokeRequest(req).map(_ => ())
  }

  /** DELETE /rest/api/2/issue/{issueIdOrKey}/remotelink
    *
    * @see https://developer.atlassian.com/cloud/jira/platform/rest/v2/#api-rest-api-2-issue-issueIdOrKey-remotelink-delete
    */
  def deleteRemoteLinkByGlobalId(issueKey: IssueKey, globalId: String): JiraRespT[Unit] = {
    implicit val rId: RequestId = RequestId.newOne("delete-remote-link-by-globalid")
    val req                     = reqGen.delete(API + s"issue/$issueKey/remotelink", KVParam("globalId", globalId))
    invokeRequest(req).map(_ => ())
  }
}
