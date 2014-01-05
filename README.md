Scala Lib For Ghost(Node.js) API
===
This is a library for scala developers to write programs that interact with the Ghost blogging platform via the REST api provided by Ghost.
Quick Example
---
First you create our ghost object with the blogs base url and authenticate.
```scala
  val ghost_client = new Ghost("http://localhost:2368/")
  ghost_client.signin("user@example.com","123456")
```

Now you need to create a <code>Posts</code> object from your <code>Ghost</code> object
```scala
val posts_examples = new Posts(ghost_client)
posts_examples.createPost("publish","Hello World","Lorem ipsum test post content.")
```
Advanced Usage
---
To get a full overview of the library and its methods, see src/test/scala/Examples.scala