package hmda.validation.rules.lar.`macro`

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.AggregateEditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.rules.lar.`macro`.MacroEditTypes._

import scala.concurrent.{ ExecutionContext, Future }

object Q061 extends AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] {

  val config = ConfigFactory.load()
  val multiplier = config.getDouble("hmda.validation.macro.Q061.numOfLarsMultiplier")

  override def name = "Q061"

  override def apply(lars: LoanApplicationRegisterSource)(implicit system: ActorSystem, materializer: ActorMaterializer, ec: ExecutionContext): Future[Result] = {

    val firstLienHoepaLoans =
      count(lars.filter(lar => lar.hoepaStatus == 1 && lar.actionTakenType == 1 && lar.lienStatus == 1 && lar.rateSpread != "NA")
        .filter(lar => lar.rateSpread.toInt >= 5))

    val total = count(lars.filter(lar => lar.actionTakenType == 1))

    for {
      f <- firstLienHoepaLoans
      t <- total
    } yield {
      f.toDouble is lessThanOrEqual(t * multiplier)
    }

  }
}
