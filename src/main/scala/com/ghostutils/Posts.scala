package com.ghostutils
import scala.util.Success
import org.json4s._
import org.json4s.native.JsonMethods._
import java.net.URLEncoder
import org.json4s.native.Serialization._
import scala.util.{Try,Success,Failure}

case class Post(
  uuid: String,
  title: String,
  markdown: String,
  html: String,
  status: String, //can be "draft" or "published"
  author_id: Int,
  created_at: String,
  created_by: Int,
  updated_at: String,
  updated_by: Int,
  slug: String,
  id: String,
  image: Option[String],
  featured: Option[Int],
  page: Option[Int],
  language: Option[String],
  meta_title: Option[String],
  meta_description: Option[String],
  author: Option[Account],
  user: Option[Account]
)


case class PostsCollection(
  posts: Option[List[Post]],
  tags: Option[List[Tag]],
  page: Int,
  limit: Int,
  pages: Int,
  total: Int,
  next: Int
)

case class Tag(
  id: Int,
  uuid: String,
  name: String,
  slug: String,
  description: Option[String],
  parent_id: Option[Int],
  meta_title: Option[String],
  meta_description: Option[String],
  created_at: String,
  created_by: Int,
  updated_at: Option[String],
  updated_by: Option[Int]
)





class Posts(ghost_host: Ghost) {
  implicit val formats = DefaultFormats

  /**
   * Creates a new post on the blog
   *
   * @author	        Chris Marlow
   * @since	          12-07-2013
   * @param	status		state of the post (draft/publish)
   * @param	title		  article title for post
   * @param	markdown	actual content (text) shown for the article
   * @return	        Left with error string or Right with Post object containing details posted
   * TODO: support adding tags to post
   */
  def createPost(status: String = "draft", title: String = "", markdown: String = ""): Either[String, Post] = {

    val response_http = ghost_host.apiJsonPost("api/v0.1/posts/", write(Map("status" -> status, "title" -> title, "markdown" -> markdown)))
    if(response_http.status.isSuccess) {
      Right(parse(response_http.body.toString()).extract[Post])
    } else {
      Left(Ghost.parseJsonError(Some(response_http.body.toString()).getOrElse("""{ "error" : "Could not fetch HTTP response" }""")))
    }

  }





  /**
   * Lists posts and allows for ordering.
   * @author          Chris Marlow
   * @since           2013-12-24
   * @param status    Status of post types to list
   * @param order_by  Sorting array for listing
   * @return          PostsCollection object with page specific posts/global post info or nothing if failed
   */
  def listPost(page: Option[Int] = None, status: String = "all",order_by: List[String] = List("updated_at","DESC")): Option[PostsCollection] = {
    val order_By = order_by.map("orderBy%5B%5D=" + _).mkString("&") + (if(page.isDefined && page.get > 0) "&page=" + page.get)
    try {
      parse(ghost_host.apiGet("api/v0.1/posts/?status=" + status + "&" + order_By).body.toString()).extractOpt[PostsCollection]
    } catch {
      case e: Exception =>
        None
    }
  }




  /**
   * Deletes a post
   * @author    Chris Marlow
   * @since     2013-12-25
   * @param id  Primary ID for post to delete
   * @return    "ok" if success or string containing error message
   */
  def deletePost(id: String): String = {
    //DELETE http://localhost:2368/api/v0.1/posts/26
    val response_http = ghost_host.apiDelete("api/v0.1/posts/" + id)
    if(response_http.status.isSuccess) {
      "ok"
    } else {
      Ghost.parseJsonError(Some(response_http.body.toString()).getOrElse("""{ "error" : "Could not fetch HTTP response" }"""))
    }
  }

  /**
   * Update a post with new json
   * @author              Chris Marlow
   * @since               2013-12-25
   * @param post_details  Post object containing primary key of post to update as well as new information for post
   * @return              Left with error string or Right with Post object having all post details
   * TODO: Add support for partial update (PATCH) if Ghost supports it (must getPost/updatePost at the moment)
   */
  def updatePost(post_details: Post): Either[String, Post] =  {
    val response_http = ghost_host.apiJsonPut("api/v0.1/posts/" + post_details.id, write(post_details))
    if(response_http.status.isSuccess) {
      Right(parse(response_http.body.toString()).extract[Post])
    } else {
      Left(Ghost.parseJsonError(Some(response_http.body.toString()).getOrElse("""{ "error" : "Could not fetch HTTP response" }""")))
    }
  }
  //PUT http://localhost:2368/api/v0.1/posts/25

  /**
   * Get the json for a single post into a hashtable
   * @author    Chris Marlow
   * @since     2013-12-25
   * @param id  Primary ID for post to get
   * @return    Left containing error string or Right containing Post object with grabbed post
   */
  def getPost(id: String): Either[String,Post] = {
    val response_http = ghost_host.apiGet("api/v0.1/posts/" + id)
    if(response_http.status.isSuccess) {
      val post_grabbed = parse(response_http.body.toString()).extractOpt[Post]
      if(post_grabbed.isEmpty)
        Left("Post could not be found.")
      else
        Right(post_grabbed.get)
    } else {
      Left(Ghost.parseJsonError(Some(response_http.body.toString()).getOrElse("""{ "error" : "Could not fetch HTTP response" }""")))
    }
  }
  //GET http://localhost:2368/api/v0.1/posts/27/

  /**
   * Gets list of all tags on server
   * @author  Chris Marlow
   * @since   2013-12-25
   * @return  List of Tag objects or None if empty
   */
  def listTags(): Option[List[Tag]] = parse(ghost_host.apiGet("api/v0.1/tags/").body.toString()).extractOpt[List[Tag]]
  //GET http://localhost:2368/api/v0.1/tags



 }