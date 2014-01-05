package com.ghostutils

import org.json4s._
import org.json4s.native.JsonMethods

object Examples {
 def main(args: Array[String]) = {
  val test_email = "test@test.com"
  val test_password = "12345678"
  val ghost_client = new Ghost("http://localhost:2368/")



  //empty fields
  println("login (bad, empty fields): " + ghost_client.signin("",""))

  //empty email
  println("login (bad, email empty): " + ghost_client.signin("",test_password))

  //empty password
  println("login (bad, password empty): " + ghost_client.signin(test_email,""))

  //invalid password
  println("login (bad, invalid password): " + ghost_client.signin(test_email,"invalid password"))

  //valid login
  println("login (good): " + ghost_client.signin(test_email,test_password))

  //re-sign in
  val ghost_login = ghost_client.signin(test_email,test_password)
  println("login (good, re-login test): " + ghost_login )


  if(ghost_login == "ok") {


    /** TEST POST API METHODS **/
    val posts_examples = new Posts(ghost_client)

    //create post
    val create_post = posts_examples.createPost("published","Hello World","Lorem ipsum the markdown goes here.")
    val post_created = create_post.right.get
    println("post create (good): " + post_created.title)

    //update post
    val post_updated_bad = posts_examples.updatePost(post_created.copy(id = "99999999"))
    println("post update (bad): " + post_updated_bad.fold[String]("error : " + _, "posted: " + _.title))


    //update post
    val post_updated_good = posts_examples.updatePost(post_created.copy(title = "title update test"))
    println("post update (good): " + post_updated_good.fold[String]("error : " + _, "posted: " + _.title))


    //get post
    println("get posted (good, newsly created): ")
    println(posts_examples.getPost(post_created.id))

    //get post
    println("get post (bad): ")
    println(posts_examples.getPost("9999999"))


    //list posts
    println("list posts (6 pages): ")
    for(page <- 0 to 5) {
      val post_info = posts_examples.listPost(Some(page)) // get post collection
      val post_summary = post_info.map(_.posts.getOrElse(List()).map(post => post.title.concat(" @ " + post.created_at))) // map to list of object summary strings (title + date)
      println("page " + page + ":\n\t" + post_summary.getOrElse(List()).mkString("\n\t") ) // show all posts with tab (directory style stdout)
    }


    //delete post
    println("delete post (good): " + posts_examples.deletePost(post_created.id))

    //delete post
    println("delete post (bad): " + posts_examples.deletePost("9999999"))

    //list tags
    println("list tags: ")
    println(posts_examples.listTags.getOrElse(List()).map(tag => tag.slug.concat(" @ " + tag.created_at)).mkString("\n\t"))


    /** TEST SETTINGS API METHODS **/

    //get blog settings
    println("get blog settings : ")
    val blog_settings = ghost_client.getBlogSettings
    println(blog_settings.get)


    //update blog settings
    println("blog settings update (bad): ")
    println(ghost_client.updateBlogSettings(blog_settings.get.copy(email = "invalid_email")))


    //update blog settings
    println("blog settings update (good): ")
    println(ghost_client.updateBlogSettings(blog_settings.get.copy(title = "Update Settings New Blog Title")))


    //get user settings
    println("get user settings: ")
    val user_settings = ghost_client.getUserSettings
    println(user_settings.get)


    //update user settings
    println("user settings update (bad): ")
    println(ghost_client.updateUserSettings(user_settings.get.copy(email = "invalid_email")))


    //update user settings
    println("user settings update (good): ")
    println(ghost_client.updateUserSettings(user_settings.get.copy(meta_title = Some("User"))))


  }

 }

}

