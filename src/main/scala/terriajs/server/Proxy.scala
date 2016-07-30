package terriajs.server

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer

class Proxy(system: ActorSystem, implicit val materializer: ActorMaterializer) {
  private val http = Http(system)

  val route =
    get(createProxy(HttpMethods.GET)) ~
    post(createProxy(HttpMethods.POST))

  def createProxy(method: HttpMethod): Route = {
    ctx => {
      val pathToProxy = ctx.unmatchedPath
      ctx.complete(http.singleRequest(HttpRequest(uri = pathToProxy.toString(), method = method)))
    }
  }
}
