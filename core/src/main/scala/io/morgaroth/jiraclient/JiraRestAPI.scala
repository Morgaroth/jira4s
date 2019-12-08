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

trait JiraRestAPI[F[_]] extends Jira4sMarshalling {
  val API = "rest/api/2/"

  implicit def m: Monad[F]

  def config: JiraConfig

  private lazy val regGen = JiraRequest.forServer(config)

  protected def invokeRequest(request: JiraRequest)(implicit requestId: RequestId): EitherT[F, JiraError, String]

  type JiraRespT[A] = EitherT[F, JiraError, A]

  /**
    * GET /rest/api/2/project/search
    *
    * @see https://developer.atlassian.com/cloud/jira/platform/rest/v2/#api-rest-api-2-project-search-get
    */
  def searchProjects: EitherT[F, JiraError, Vector[JiraProject]] = {
    implicit val rId: RequestId = RequestId.newOne("search-projects")
    val req = regGen(Methods.Get, API + "projects/search", Nil, None)
    invokeRequest(req).flatMap(MJson.readT[F, Vector[JiraProject]])
  }

  /** GET /rest/api/2/issue/{issueIdOrKey}
    *
    * @see https://developer.atlassian.com/cloud/jira/platform/rest/v2/#api-rest-api-2-issue-issueIdOrKey-get
    */
  def getIssue(key: String): JiraRespT[JiraIssue] = {
    implicit val rId: RequestId = RequestId.newOne("get-issue-by-key")
    val req = regGen(Methods.Get, API + s"issue/$key", Nil, None)
    invokeRequest(req).flatMap(MJson.readT[F, JiraIssue])
  }

  /** GET /rest/api/2/issue/{issueIdOrKey}
    *
    * @see https://developer.atlassian.com/cloud/jira/platform/rest/v2/#api-rest-api-2-search-get
    * @see https://confluence.atlassian.com/jirasoftwarecloud/advanced-searching-764478330.html
    */
  def searchIssues(
                    query: JqlQEntry
                  ): EitherT[F, JiraError, Vector[JiraIssue]] = {

    def getPage(start: Int = 0): EitherT[F, JiraError, JiraPaginatedIssues] = {
      implicit val rId: RequestId = RequestId.newOne(s"search-issues-page-$start")
      val req = regGen(Methods.Get, API + "search",
        List(Jql(query), JPage(start, 50)),
        None
      )
      invokeRequest(req).flatMap(MJson.readT[F, JiraPaginatedIssues])
    }

    def getAll(startFrom: Int, acc: Vector[JiraIssue]): JiraRespT[Vector[JiraIssue]] = {
      getPage(startFrom).flatMap { resp =>
        val currentIssues = acc ++ resp.issues
        val currentCnt = currentIssues.size
        if (currentCnt > resp.total || resp.issues.isEmpty) {
          currentIssues.pure[JiraRespT]
        } else {
          getAll(startFrom + resp.issues.length, currentIssues)
        }
      }
    }

    getAll(0, Vector.empty)
  }

  /** GET /rest/api/2/issue/{issueIdOrKey}/remotelink
    *
    * @see https://developer.atlassian.com/cloud/jira/platform/rest/v2/#api-api-2-issue-issueIdOrKey-remotelink-get
    */
  def getIssueRemoteLinks(key: String): EitherT[F, JiraError, Vector[JiraRemoteLink]] = {
    implicit val rId: RequestId = RequestId.newOne("get-issue-remote-links")
    val req = regGen(Methods.Get, API + s"issue/$key/remotelink", Nil, None)
    invokeRequest(req).flatMap(MJson.readT[F, Vector[JiraRemoteLink]])
  }

  /** POST /rest/api/2/issue/{issueIdOrKey}/remotelink
    *
    * @see https://developer.atlassian.com/cloud/jira/platform/rest/v2/#api-api-2-issue-issueIdOrKey-remotelink-post
    */
  def createOrUpdateIssueLink(
                               issueKey: String, linkId: String,
                               link: String, title: String, resolved: Boolean,
                               icon: Option[Icon] = None, relationship: Option[Relationship] = None)
  : EitherT[F, JiraError, RemoteIssueLinkIdentifies] = {
    implicit val rId: RequestId = RequestId.newOne("create-or-update-issue-link")
    val req = regGen(Methods.Post, API + s"issue/$issueKey/remotelink", Nil, MJson.write(
      CreateJiraRemoteLink(linkId, None, relationship.map(_.raw),
        RemoteLinkObject(link, title, None, icon, JiraRemoteLinkStatus(resolved.some, None).some))
    ).some)

    invokeRequest(req).flatMap(MJson.readT[F, RemoteIssueLinkIdentifies])
  }
}

//  private def doJiraRequest[T](name: String, api: String, path: String, query: JiraQuery*)(implicit encoder: Decoder[T], jCfg: JiraConfig): Either[T] = {
//    val req = JiraRequest(jCfg.address, api + path, query.toList)
//    logger.info(s"Jira request [$name]: ${req.render}")
//    val response = EitherT(doRequest[T](HttpRequest(uri = req.render).withHeaders(List(RawHeader("Authorization", jCfg.getBasicAuthHeaderValue))), name)) leftMap {
//      failure => JiraFailure(failure.toString)
//    }
//    JiraResponse(response.value)
//  }
//
//  def getAllProjects(implicit jCfg: JiraConfig): JiraResponse[List[JProject]] = doJiraRequest[List[JProject]]("all-projects", API, "/project")

//  def getUserByEmail(email: String)(implicit jCfg: JiraConfig): JiraResponse[JUser] = {
//    doJiraRequest[List[JUser]]("get-user", API, s"/user/search/?username=$email").toDashboard.subflatMap {
//      posisble =>
//        if (posisble.lengthCompare(1) > 1) {
//          logger.warn(s"Multiple users returned for $email")
//        } else if (posisble.lengthCompare(0) == 0) {
//          logger.error(s"no user for $email at ${jCfg.jiraId}")
//        }
//        posisble.headOption.map(_.asRight[JiraClientError]).getOrElse(UserNotFound(email).asInstanceOf[JiraClientError].asLeft[JUser])
//    }
//  }


//  def fetchWorklogsOfIssue(i: JIssue, user: String, startingFrom: Int, max: Int)(implicit jCfg: JiraConfig): JiraResponse[Vector[JWorklog]] = {
//
//
//    def getPage(start: Int) = {
//      doJiraRequest[JiraPaginatedWorklog](s"issue-worklogs-$user", API, s"/issue/${i.id}/worklog",
//        JPage(start, 50)
//      ).toDashboard
//    }
//
//    def getAll(startFrom: Int, acc: Vector[JWorklog]): JiraResponse[Vector[JWorklog]] = {
//      getPage(startFrom).flatMap { resp =>
//        val currentIssues = acc ++ resp.worklogs
//        val currentCnt = currentIssues.size
//        if (currentCnt >= resp.total || currentCnt >= max || resp.worklogs.isEmpty || (resp.startAt < startFrom && resp.total == resp.maxResults && resp.total == resp.worklogs.size)) {
//          currentIssues.pure[JiraResponse]
//        } else {
//          getAll(startFrom + 50, currentIssues)
//        }
//      }
//    }
//
//    getAll(startingFrom, Vector.empty)
//  }
//
//  def fetchIssuesWithWorklogsOfUser(username: String, startingFrom: Int, max: Int, newerThan: DateTime)(implicit jCfg: JiraConfig): JiraResponse[Vector[JIssue]] = {
//    def getPage(start: Int) = {
//      doJiraRequest[JiraPaginatedIssuesWork]("user-issues", API, "/search",
//        JPage(start, 50),
//        JFields("project,worklog"),
//        Jql("worklogAuthor" === username.toUrlEncoded and "worklogDate".gte(newerThan.toString("yyyy-MM-dd")))
//      ).toDashboard
//    }
//
//    def getAll(startFrom: Int, acc: Vector[JIssue]): JiraResponse[Vector[JIssue]] = {
//      getPage(startFrom).flatMap { resp =>
//        val currentIssues = acc ++ resp.issues
//        val currentCnt = currentIssues.size
//        if (currentCnt > resp.total || currentCnt >= max || resp.issues.isEmpty) {
//          currentIssues.pure[JiraResponse]
//        } else {
//          getAll(startFrom + 50, currentIssues)
//        }
//      }
//    }
//
//    getAll(startingFrom, Vector.empty)
//  }
//

//  def fetchIssuesByKeys(keys: Vector[String])(implicit jCfg: JiraConfig): JiraResponse[Vector[JIssue]] = {
//    def getPage(keysToFetch: Vector[String]): JiraResponse[JiraPaginatedIssuesWork] = {
//      doJiraRequest[JiraPaginatedIssuesWork]("issues-by-keys", API, "/search",
//        JPage(0, 50),
//        JFields("project,worklog"),
//        Jql("id" in keysToFetch.toSet)
//      ).toDashboard.map { resp =>
//        if (resp.issues.length != keysToFetch.size) logger.error(s"DIFFERENT RESULT THAN REQUESTED ${keysToFetch.size} ${resp.issues.size}")
//        resp
//      }
//
//    }
//
//    if (keys.isEmpty) Vector.empty[JIssue].pure[JiraResponse]
//    else keys.distinct.grouped(50).toVector.traverse(getPage).map(_.flatMap(_.issues))
//  }

//  def fetchWorklogsOfUserFromTempoAPI(username: String, from: DateTime, to: DateTime)(implicit jCfg: JiraConfig): JiraResponse[Vector[JTempoWorklog]] = {
//    doJiraRequest[Vector[JTempoWorklog]](s"tempo-worklogs-$username", TEMPO, "/worklogs/",
//      JTUser(username), JTFrom(from.toString("yyyy-MM-dd")), JTTo(to.toString("yyyy-MM-dd"))
//    ).toDashboard
//  }