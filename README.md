<p align="center">
<img src="https://github.com/d-exclaimation/ahql/blob/main/icon.png" width="175" alt="logo" style="margin:1rem;"/>
</p>
<p align="center"> <h1>ahql</h1></p>


Akka Http Query Library, minimal GraphQL client and server exposing as a set of akka-http utilities.

## Setup

**Latest Version**: `0.2.0`

```sbt
"io.github.d-exclaimation" %% "ahql" % latestVersion
```

## Usage/Examples

<details>
<summary>
	<big>Server Example</big>
</summary>

#### Using a Server instance

```scala
object Main extends SprayJsonSupport {
  implicit val system: ActorSystem[Nothing] = 
    ActorSystem(Behaviors.empty, "--")

  val gqlServer: Ahql.Server[Context, Unit] = 
    Ahql.createServer[Context, Unit](schema, ())

  val route: Route = path("graphql") {
    optionalHeaderValueByName("Authorization")) { auth =>
      val context = Context(auth)
      gqlServer.applyMiddleware(context)
    }
  }
}
```

#### Using a shorthand 

```scala
object Main extends SprayJsonSupport {
  implicit val system: ActorSystem[Nothing] = 
    ActorSystem(Behaviors.empty, "--")

  val route: Route = path("graphql") {
    optionalHeaderValueByName("Authorization")) { auth =>
      val context = Context(auth)
      Ahql.applyMiddleware[Context, Unit](schema, context), ())
    }
  }
}
```

Both will gave out two endpoints

```http
POST: ".../graphql"
GET: ".../graphql"
```

</details>


<details>
<summary>
	<big>Client Example</big>
</summary>

#### Using a Client instance

```scala
object Main extends SprayJsonSupport {
  implicit val system: ActorSystem[Nothing] = 
    ActorSystem(Behaviors.empty, "--")

  val gqlClient: Ahql.Client = 
    Ahql.createClient("http://localhost:4000/graphql")

  val query: ast.Document = graphql"""
    query {
      someField {
        nested1
        nested2
      }
    }
  """

  val GqlResponse(data, errors) = gqlClient.fetch(query, 
    headers = headers.Authorization("Bearer token") :: Nil
  )
  // data: Option[JsObject]
  // errors: Option[Vector[JsObject]]
}
```

#### Using a shorthand 

```scala
object Main extends SprayJsonSupport {
  implicit val system: ActorSystem[Nothing] = 
    ActorSystem(Behaviors.empty, "--")

  val query: ast.Document = graphql"""
    query {
      someField {
        nested1
        nested2
      }
    }
  """

  val GqlResponse(data, errors) = Ahql
    .fetch(
      endpoint = "http://localhost:4000/graphql",
      query = query, 
      headers = headers.Authorization("Bearer token") :: Nil
    )
  // data: Option[JsObject]
  // errors: Option[Vector[JsObject]]
}
```

</details>

<br/>

<!-- - [Getting Started](https://overlayer.netlify.app/docs/intro) -->

### Feedback

If you have any feedback, feel free to reach out through the issues tab or through my
Twitter [@d_exclaimation](https://twitter.com/d_exclaimation).
