package openday

import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.http.scaladsl.Http
import org.apache.pekko.http.scaladsl.marshalling.{Marshaller, ToEntityMarshaller}
import org.apache.pekko.http.scaladsl.model.*
import org.apache.pekko.http.scaladsl.model.StatusCodes.{BadRequest, Forbidden, InternalServerError, NotFound}
import org.apache.pekko.http.scaladsl.model.headers.{HttpCookie, SameSite}
import org.apache.pekko.http.scaladsl.server.Directives.*
import org.apache.pekko.http.scaladsl.server.{ExceptionHandler, Route}
import org.apache.pekko.http.scaladsl.unmarshalling.{FromRequestUnmarshaller, Unmarshaller}

import com.wbillingsley.handy.*

import scala.io.StdIn
import org.slf4j.LoggerFactory

import java.lang.Runtime
import scala.concurrent.*
import scala.util.control.NonFatal

private val random = new java.security.SecureRandom()
private def randomSessionId() = {
  new java.math.BigInteger(120, random).toString(32)
}
private def randomSessionCookie() = HttpCookie.apply(
  name="assessorySession",
  value=randomSessionId(),
  path=Some("/")
).withSameSite(SameSite.Lax)

given FromRequestUnmarshaller[Language] = Unmarshaller.messageUnmarshallerFromEntityUnmarshaller(
    Unmarshaller.stringUnmarshaller
      .forContentTypes(MediaTypes.`application/json`)
      .flatMap(
        ctx => mat => json => try {
          Future.successful(fromJson(json))
        } catch {
          case x => Future.failed(x)
        }
      )
  )

given ToEntityMarshaller[Language] =
  Marshaller.withFixedContentType(MediaTypes.`application/json`) { a =>
    HttpEntity(MediaTypes.`application/json`, toJson(a))
  }

val logger = LoggerFactory.getLogger("org.assessory.akkahttp.main")

given ExceptionHandler = ExceptionHandler {
  case _:NoSuchElementException =>
    complete(HttpResponse(NotFound))
  // case UserError(msg) =>
  //   complete(HttpResponse(BadRequest, entity=msg))
  case NonFatal(e) =>
    logger.error("Completed response with Internal Server Error", e)
    complete(HttpResponse(InternalServerError, entity =
      s"""ERROR: ${e.getMessage}
         |
         |${e.getStackTrace.map(_.toString).mkString("\n")}
         |""".stripMargin))
}



@main def startServer(port:Int = 8080, stopOnReturn:Boolean = false) = {
  given system:ActorSystem[Any] = ActorSystem(Behaviors.empty, "assessory-system")
  given ec:ExecutionContext = system.executionContext

  /* Start-up config */
  println(s"Port is $port")

  // Set the execution context (ie the thread pool) that RefFuture work should happen on
  RefFuture.executionContext = ec
 
  val route = Route.seal(concat(
    path("ping") {
      get {
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "pong"))
      }
    },

    // Force the session cookie to be set on load of index.html
    pathSingleSlash {
      get {
        optionalCookie("assessorySession") {
          case Some(sessionCookie) =>
            encodeResponse {
              getFromResource("static/index.html")
            }

          case None =>
            setCookie(randomSessionCookie()) {
              encodeResponse {
                getFromResource("static/index.html")
              }
            }
        }
      }
    },

    path("api" / "call") {
      post {
        decodeRequest {
          extractClientIP { ip =>
            entity(as[Language]) { call =>
              optionalCookie("assessorySession") {
                case Some(sessionCookie) =>
                  complete {
                    call match {
                      case _ => Language.Pong
                    }
                  }

                case None =>
                  val cookie = randomSessionCookie()

                  setCookie(cookie) {
                    complete {
                      call match {
                        case _ => Language.Pong
                      }
                    }
                  }
              }

            }

          }

        }
      }
    },

    pathPrefix("assets" / Remaining) { file =>
      // optionally compresses the response with Gzip or Deflate
      // if the client accepts compressed responses
      encodeResponse {
        getFromResource(file)
      }
    },

    pathPrefix("static" / Remaining) { file =>
      // optionally compresses the response with Gzip or Deflate
      // if the client accepts compressed responses
      encodeResponse {
        getFromResource("static/" + file)
      }
    },

    pathPrefix("assets" / Remaining) { file =>
      // optionally compresses the response with Gzip or Deflate
      // if the client accepts compressed responses
      encodeResponse {
        getFromResource("public/" + file)
      }
    },

    path("newcookie") {
      get {
        val cookie = randomSessionCookie()
        setCookie(cookie) {
          complete {
            s"New cookie is ${cookie.value()}"
          }
        }
      }
    }

  ))

  val bindingFuture = Http().newServerAt("localhost", port).bind(route)

  if (stopOnReturn) then
    println(s"Server online at http://localhost:$port/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  else
    println(s"Server online at http://localhost:$port/")
    Runtime.getRuntime.addShutdownHook(new Thread() {
      override def run:Unit =
        for exited <- system.whenTerminated do System.exit(0)
        system.terminate()
    })
}