package terriajs.server

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer

class Proxy(system: ActorSystem, implicit val materializer: ActorMaterializer) {
  private val http = Http(system)
  private implicit val executionContext = system.dispatcher

  private val requestHeadersToRemove = Set(
    "timeout-access",
    "host",
    "x-forwarded-host",
    "proxy-connection",
    "connection",
    "keep-alive",
    "te",
    "proxy-authorization",
    "upgrade"
  )

  private val responseHeadersToRemove = Set(
    "proxy-connection",
    "connection",
    "expires",
    "pragma",
    "transfer-encoding",
    "trailer",
    "proxy-authenticate"
  )

    val route = (get | post) {
      (path(Remaining) & extractRequest) { (url, request) =>
        complete {
          http.singleRequest(HttpRequest(
            uri = url,
            method = request.method,
            headers = request.headers.filter(header => !requestHeadersToRemove.contains(header.lowercaseName()))
          )).map(response => {
            response.withHeaders(response.headers.filter(header => !responseHeadersToRemove.contains(header.lowercaseName())))
          })
        }
      }
    }
}
