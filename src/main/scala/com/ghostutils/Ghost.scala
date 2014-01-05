package com.ghostutils

import uk.co.bigbeeconsultants.http.HttpClient
import uk.co.bigbeeconsultants.http.response.Response
import scala.collection.immutable._
import uk.co.bigbeeconsultants.http.header.CookieJar
import java.net.{URL, URLEncoder}
import scala.Predef._
import scala.collection.immutable.Map
import scala.Some
import org.json4s._
import org.json4s.native.JsonMethods._
import scala.Some
import org.json4s.JsonDSL._
import org.json4s.native.Serialization._

case class Account(
  id: Int,
  uuid: String,
  name: String,
  slug: String,
  email: String,
  image: Option[String],
  cover: Option[String],
  bio: Option[String],
  website: Option[String],
  location: Option[String],
  accessibility: Option[String],
  status: Option[String],
  language: Option[String],
  meta_title: Option[String],
  meta_description: Option[String],
  last_login: Option[String],
  created_at: String,
  updated_at: Option[String]
) extends Serializable


case class BlogSettings(
  title: String,
  description: String,
  email: String,
  logo: Option[String],
  cover: Option[String],
  defaultLang: String,
  postsPerPage: String,
  forceI18n: String,
  activeTheme: String,
  availableThemes: List[Theme]
) extends Serializable

case class Theme(
  name: String,
  active: Boolean
) extends Serializable




object Ghost {

  /**
   * Parse CSRF Token from HTTP Cookie for form posting.
   * @author      Chris Marlow
   * @since       2013-12-24
   * @param node  Ghost connection endpoint
   * @return      Big Bee HTTP header to use in request
   */
  def buildReqHeader(node: GhostPoint): uk.co.bigbeeconsultants.http.header.Headers = {
    val pattern = """%22_csrf%22%3A%22(.*?)%22""".r
    if(node.cookie_jar.get("connect.sess").isDefined) {
      val header = uk.co.bigbeeconsultants.http.header.Headers(
        Map(
          "X-CSRF-Token" -> Some(
            pattern.findFirstMatchIn(
              node.cookie_jar.get("connect.sess").get.value
            ).get.group(1)
          ).getOrElse("")
        )
      )
      header

    } else {
      uk.co.bigbeeconsultants.http.header.Headers.Empty
    }
  }


  /**
   * HTTP POST API call to REST endpoint
   * @author	            Chris Marlow
   * @since 	            2013-12-08
   * @param node          Ghost connection endpoint
   * @param	endpoint		  Method on API to call
   * @param	body			    Input body/params for API call
   * @param	content_type	MIME type to send for API body content
   * @return	            HTTP Response from the api server
   */

  def apiPost(node: GhostPoint, endpoint: String, body: String, content_type : String): Response = {

    val post_resp = node.api_client.post(
      new java.net.URL(node.url + endpoint ), // url path
      Some(
        uk.co.bigbeeconsultants.http.request.RequestBody( // create POST body
          body,
          uk.co.bigbeeconsultants.http.header.MediaType(content_type).withCharset("UTF-8")// add content-type
        )
      ),
      buildReqHeader(node),
      node.cookie_jar
    )
    post_resp
  }


  /**
   * HTTP PUT API call to REST endpoint
   * @author	            Chris Marlow
   * @since 	            2013-12-08
   * @param node          Ghost connection endpoint
   * @param	endpoint		  Method on API to call
   * @param	body			    Input body/params for API call
   * @param	content_type	MIME type to send for API body content
   * @return	            HTTP Response from the api server
   */

  def apiPut(node: GhostPoint, endpoint: String, body: String, content_type : String): Response = {

    val post_resp = node.api_client.put(
      new java.net.URL(node.url + endpoint ), // url path
        uk.co.bigbeeconsultants.http.request.RequestBody( // create POST body
          body,
          uk.co.bigbeeconsultants.http.header.MediaType(content_type).withCharset("UTF-8")// add content-type
        ),
      buildReqHeader(node),
      node.cookie_jar
    )
    post_resp
  }


  /**
   * HTTP POST API call to REST endpoint
   * @author	            Chris Marlow
   * @since 	            2013-12-08
   * @param node          Ghost connection endpoint
   * @param	endpoint		  Method on API to call
   * @param	body			    Input body/params for API call
   * @param	content_type	MIME type to send for API body content
   * @return	            HTTP Response from the api server
   */
  def apiPost(node: GhostPoint, endpoint: String, body: Map[String,String], content_type : String): Response =
    apiPost(node,endpoint,mapToPostBody(body),content_type)


  /**
   * HTTP PUT API call to REST endpoint
   * @author	            Chris Marlow
   * @since 	            2013-12-08
   * @param node          Ghost connection endpoint
   * @param	endpoint		  Method on API to call
   * @param	body			    Input body/params for API call
   * @param	content_type	MIME type to send for API body content
   * @return	            HTTP Response from the api server
   */
  def apiPut(node: GhostPoint, endpoint: String, body: Map[String,String], content_type : String): Response =
    apiPut(node,endpoint,mapToPostBody(body),content_type)


  /**
   * Calls method on API using GET request
   * @author          Chris Marlow
   * @since           2013-12-24
   * @param node      Ghost connection endpoint
   * @param endpoint  Method on API to call
   * @return          HTTP Response from the api server
   */
  def apiGet(node: GhostPoint, endpoint: String): Response = {
    val post_rep = node.api_client.get(
      new URL(node.url + endpoint),
      buildReqHeader(node),
      node.cookie_jar
    )
    post_rep
  }

  /**
   * Calls method on API using DELETE request
   * @author          Chris Marlow
   * @since           2013-12-25
   * @param node      Ghost connection endpoint
   * @param endpoint  Method on API to call
   * @return          HTTP Response from the api server
   */
  def apiDelete(node: GhostPoint, endpoint: String): Response = {
    val post_resp = node.api_client.delete(
      new URL(node.url.concat(endpoint)),
      buildReqHeader(node),
      node.cookie_jar
    )
    post_resp
  }

  /**
   * Creates a string to represent the HTTP POST body of a scala Map
   * @author      Chris Marlow
   * @since       2013-12-24
   * @param body  Input body/params for API call
   * @return      HTTP Response from the api server
   */
  def mapToPostBody(body: Map[String,String]) = body.map(inputs => inputs._1 + "=" + URLEncoder.encode(inputs._2,"UTF-8") ).mkString("&")


  /**
   * @author      Chris Marlow
   * @since       2013-01-03
   * @param json  String of json containing error
   * @return      Default error message or parsed from JSON
   */
  def parseJsonError(json: String): String = {
    val parsed_json =  Some((parse(json)\"error").values).getOrElse("Could not fetch JSON.").toString() //  fetch json error
    parsed_json
  }
}


case class GhostPoint(api_client: HttpClient, cookie_jar: CookieJar, url: String)

class Ghost(url_endpoint: String) {

  implicit val formats = DefaultFormats + FieldSerializer[BlogSettings]() + FieldSerializer[Account]()

  var api_cookie_jar = uk.co.bigbeeconsultants.http.header.CookieJar()
  val api_client = new uk.co.bigbeeconsultants.http.HttpClient(
    new uk.co.bigbeeconsultants.http.Config(
      5000,
      10000,
      true,
      3,
      true,
      true,
      Some("ScalaGhostLib"),
      java.net.Proxy.NO_PROXY,
      uk.co.bigbeeconsultants.http.auth.CredentialSuite.empty,
      None,
      None,
      uk.co.bigbeeconsultants.http.header.Headers.Empty,List.empty
    )
  )

  /**
   * build object with cookie jar in the moment and url end point
   * @author  Chris Marlow
   * @since   2013-12-24
   * @return  Object containing snapshot of endpoint at that moment in time
   */

  def ghost_node = new GhostPoint(api_client,api_cookie_jar,url_endpoint)

  /**
   * quick function definers for form and json posts with map and string bodies
   * @author	        Chris Marlow
   * @since 	        2013-12-08
   * @param	endpoint	Method on API to call
   * @param	body			Input body/params for API call
   * @return	        HTTP Response from the api server
   */
  def apiFormPost(endpoint: String, body: String) = {
    val api_response = Ghost.apiPost(ghost_node, endpoint, body,"application/x-www-form-urlencoded")
    api_cookie_jar = api_response.cookies.getOrElse(uk.co.bigbeeconsultants.http.header.CookieJar())
    api_response
  }

  def apiJsonPost(endpoint: String, body: String) =  {
    val api_response = Ghost.apiPost(ghost_node, endpoint, body, "application/json")
    api_cookie_jar = api_response.cookies.getOrElse(uk.co.bigbeeconsultants.http.header.CookieJar())
    api_response
  }
  def apiJsonPut(endpoint: String, body: String) = {
    val api_response = Ghost.apiPut(ghost_node, endpoint, body, "application/json")
    api_cookie_jar = api_response.cookies.getOrElse(uk.co.bigbeeconsultants.http.header.CookieJar())
    api_response
  }

  def apiFormPost(endpoint: String, body: Map[String,String]): Response = apiFormPost(endpoint,Ghost.mapToPostBody(body))
  def apiJsonPost(endpoint: String, body: Map[String,String]): Response = apiJsonPost(endpoint,Ghost.mapToPostBody(body))
  def apiJsonPut(endpoint: String, body: Map[String,String]): Response = apiJsonPut(endpoint,Ghost.mapToPostBody(body))
  /**
   * quick function definers for form and json posts with map and string bodies
   * @author	        Chris Marlow
   * @since 	        2013-12-24
   * @param	endpoint	Method on API to call
   * @return	        HTTP Response from the api server
   */
  def apiGet(endpoint: String) = {
    val resp = Ghost.apiGet(ghost_node, endpoint)
    api_cookie_jar = resp.cookies.getOrElse(uk.co.bigbeeconsultants.http.header.CookieJar())
    resp
  }


  /**
   * quick function definers for form and json posts with map and string bodies
   * @author	        Chris Marlow
   * @since 	        2013-12-24
   * @param	endpoint	Method on API to call
   * @return	        HTTP Response from the api server
   */
  def apiDelete(endpoint: String) = {
    val resp = Ghost.apiDelete(ghost_node, endpoint)
    api_cookie_jar = resp.cookies.getOrElse(uk.co.bigbeeconsultants.http.header.CookieJar())
    resp
  }


  /**
   * Sends the login credentials to the api login, sets cookie for future api calls
   *
   * @author	        Chris Marlow
   * @since	          2013-12-07
   * @param	email		  users email that you want to authenticate
   * @param	password	password for user
   * @return	        string with "ok" for true, otherwise error message
   */


  def signin(email: String, password: String): String = {
    //Add sleep to force delay between auth attempts
    Thread.sleep(5000)

    // initialize session and gather CSRF token
    if(api_cookie_jar.get("connect.sess").isEmpty) {
      apiGet("ghost/signin/")
    } else {
      signout
    }

    // send login information
    val response_http = apiFormPost("ghost/signin/", "email=" + URLEncoder.encode(email,"UTF-8") + "&password=" + password)
    if(!response_http.status.isSuccess) {
      Ghost.parseJsonError(Some(response_http.body.toString()).getOrElse("""{ "error" : "Could not fetch HTTP response" }"""))
    } else {
      "ok"
    }
  }

  /**
   * logs the user out by sending a GET to signout
   * @author  Chris Marlow
   * @since   2013-12-24
   * @return  response success for signout call
   */
  def signout: Boolean = {
    val resp = apiGet("signout/")
    resp.status.isSuccess
  }

  /**
   * Gets a map containing each options and its values
   * @author  Chris Marlow
   * @since   2013-12-25
   * @return  BlogSettings object containing all the blogs settings
   */
  def getBlogSettings(): Option[BlogSettings] = parse(apiGet("api/v0.1/settings/?type=blog,theme").body.toString()).extractOpt[BlogSettings]
  //GET http://localhost:2368/api/v0.1/settings/?type=blog,theme


  /**
   * Updates blog settings (new cover, title, etc) with json values
   * @author          Chris Marlow
   * @since           2013-12-25
   * @param new_json  BlogSettings object with update details and primary key info of post to update
   * @return          Left with error string or Right with updated BlogSettings object
   * TODO: add upload of photo functionality
   */
  def updateBlogSettings(new_json: BlogSettings): Either[String,BlogSettings] =  {
    val response_http = apiJsonPut("api/v0.1/settings/?type=blog,theme", write(new_json))
    if(response_http.status.isSuccess) {
      val settings_grabbed = parse(response_http.body.toString()).extractOpt[BlogSettings]
      if(settings_grabbed.isDefined)
        Right(settings_grabbed.get)
      else
        Left("Could not parse settings JSON")
    } else {
      Left(Ghost.parseJsonError(Some(response_http.body.toString()).getOrElse("""{ "error" : "Could not fetch HTTP response" }""")))
    }
  }
  //PUT http://localhost:2368/api/v0.1/settings/?type=blog,theme


  /**
   * Get user settings containing options and values
   * @author  Chris Marlow
   * @since   2013-12-25
   * @return  Account object with account information
   */
  def getUserSettings(): Option[Account] = parse(apiGet("api/v0.1/users/me/").body.toString()).extractOpt[Account]
  //GET http://localhost:2368/api/v0.1/users/me/

  /**
   * Update a user settings with PUT request
   * @author          Chris Marlow
   * @since           2013-12-25
   * @param new_json  Account object containing user primary key and new user information
   * @return          Left containing error string or Right with new Account object
   * TODO: Add upload photo functionality
   */
  def updateUserSettings(new_json: Account): Either[String,Account] = {
    val response_http = apiJsonPut("api/v0.1/users/me/", write(new_json))
    if(response_http.status.isSuccess) {
      val updated_settings = parse(response_http.body.toString()).extractOpt[Account]
      if(updated_settings.isDefined)
        Right(updated_settings.get)
      else
        Left("Could not parse settings JSON")
    } else {
      Left(Ghost.parseJsonError(Some(response_http.body.toString()).getOrElse("""{ "error" : "Could not fetch HTTP response" }""")))
    }

  }
  //PUT http://localhost:2368/api/v0.1/users/me/


}
