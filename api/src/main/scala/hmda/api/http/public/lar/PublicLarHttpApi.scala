package hmda.api.http.public.lar

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.model.HttpEntity.ChunkStreamPart
import akka.http.scaladsl.model.{ ContentTypes, HttpEntity, HttpResponse, MediaTypes }
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import hmda.api.http.HmdaCustomDirectives
import hmda.query.DbConfiguration
import hmda.query.repository.filing.FilingComponent

import scala.concurrent.ExecutionContext

trait PublicLarHttpApi extends HmdaCustomDirectives with FilingComponent with DbConfiguration {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  val log: LoggingAdapter

  val modifiedLarRepository = new ModifiedLarRepository(config)

  def modifiedLar(institutionId: String)(implicit ec: ExecutionContext) =
    pathPrefix("filings" / Segment) { period =>
      path("lar") {
        timedGet { _ =>
          val data = modifiedLarRepository.findByRespondentIdSource(institutionId, period)
            .map(x => ChunkStreamPart(x.toCSV + "\n"))
          val response = HttpResponse(entity = HttpEntity.Chunked(ContentTypes.`text/csv(UTF-8)`, data))
          complete(response)
        }
      }
    }

}
