package de.telefonica

import akka.actor.ActorSystem
import akka.event.NoLogging
import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import de.telefonica.services.{Column, JsonService, Row, Tables}
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.Future
import scala.concurrent.duration._


class FullTestKitExampleSpec extends WordSpec with Matchers with ScalatestRouteTest with JsonService { //with RepositoryTestImpl {

  implicit def default(implicit system: ActorSystem) = RouteTestTimeout(3.second)
  override val logger = NoLogging

  "The service" should {

    "return a list of user tables for GET requests to the root path" in {
      // tests:
      Get("/api/v1/") ~> route ~> check {
        status shouldBe OK
        contentType shouldBe `application/json`
        responseAs[String] shouldEqual  """{"tables":["digitalx:lkp_msisdn","digitalx:profiler2","test"]}"""
      }
    }

    "return a row with selcted columns for GET requests to the /api/v1/digitalx:profiler2/1234567/d:cli/d:job/d:family path" in {
      // tests:
      Get("/api/v1/digitalx:profiler2/1234567/d:cli/d:job/d:family") ~> route ~> check {
        status shouldBe OK
        contentType shouldBe `application/json`
        responseAs[String] shouldEqual  """{"rowkey":"1234567","columns":[{"family":"d","qualifier":"cli","value":"Max Mustermann"},{"family":"d","qualifier":"family","value":"single"},{"family":"d","qualifier":"job","value":"Arbeiter"}]}"""
      }
    }

    "leave GET requests to other paths unhandled" in {
      // tests:
      Get("/") ~> route ~> check {
        handled shouldBe false
      }
    }

    "return a MethodNotAllowed error for PUT requests to the root path" in {
      // tests:
      Put("/api/v1/") ~> Route.seal(route) ~> check {
        status shouldEqual StatusCodes.MethodNotAllowed
        responseAs[String] shouldEqual "HTTP method not allowed, supported methods: GET"
      }
    }
  }

}


// Just For testing without repo
trait RepositoryTestImpl extends JsonService {
  override def getUserTables(): Future[Tables] = Future.successful(Tables(List("digitalx:lkp_msisdn", "digitalx:profiler2", "test")))
  override def getSingleRowColumnValues(table: String, row: String, cols: List[String]): Future[Row] = Future.successful(Row("1234567", List(Column("d","cli","Max Mustermann"), Column("d","family","single"), Column("d","job","Arbeiter"))))
}