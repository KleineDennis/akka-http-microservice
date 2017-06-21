package de.telefonica.services

import akka.event.LoggingAdapter
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives
import de.telefonica.repo.HBaseRepository
import spray.json.DefaultJsonProtocol

import scala.concurrent.Future


/**
  * Created by denniskleine on 10.05.17.
  */
// domain model
final case class Tables(tables: List[String])
final case class Version(version: String)
final case class Server(version: String, master: String, avgload: Double, clusterId: String, servers: List[String], requests: Int)
final case class Column(family: String, qualifier: String, value: String)
final case class Row(rowkey: String, columns: List[Column])

// collect your json format instances into a support trait:
trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
//  implicit val printer = PrettyPrinter
  implicit val tables = jsonFormat1(Tables)
  implicit val version = jsonFormat1(Version)
  implicit val server = jsonFormat6(Server)
  implicit val column = jsonFormat3(Column)
  implicit val rowkey = jsonFormat2(Row)
}

trait JsonService extends Directives with JsonSupport  {
  val logger: LoggingAdapter
  val repo = new HBaseRepository

  def getClusterVersion(): Future[Version] = repo.version
  def getClusterStatus(): Future[Server] = repo.status
  def getUserTables(): Future[Tables] = repo.list
  def getSingleRowColumnValues(table: String, row: String, cols: List[String]): Future[Row] = repo.row(table, row, cols)
//  def getSingleRowColumnValues(table: String, row: String, cols: List[String]): Row = Await.result(repo.row(table, row, cols), 3 second)


  /**
    * Reference: https://hbase.apache.org/book.html#_rest
    *
    *  GET /api/v1/                   - List of all non-system tables
    *
    * Cluster-Wide Endpoints
    *  GET /api/v1/version/cluster    - Version of HBase running on this cluster
    *  GET /api/v1/status/cluster     - Cluster status
    *  GET /api/v1/                   - List of all non-system tables
    *
    * Endpoints for Get Operations
    *  GET /api/v1/{table}/{row}      - Get all columns of a single row
    *  GET /api/v1/{table}/{row}/{column:qualifier} - Get the value of a single column
    *  GET /api/v1/{table}/{row}/{col1, col2,..., coln}  - Get values from all requested columns of a single row      --deprecated wird ersetzt durch  GET /api/v1/{table}/{row}?filtered
    *
    * Endpoints for Scan Operations
    *  PUT /api/v1//{table}/scanner   - Get a Scanner object
    *  GET /api/v1/{table}/scanner/{scannerId}  - Get the next batch from the scanner
    *  DELETE /api/v1/{table}/scanner/{scannerId} - Deletes the scanner and frees the resources it used
    *
    * Endpoints for Put Operations
    *  PUT /api/v1/{table}/{rowkey}   - Write a row to a table
    */
  val route =
      pathPrefix("api" / "v1") {
          get {
            pathSingleSlash {
              logger.info("List of all non-system tables")
              complete(getUserTables)
            } ~
              path("ping") {
                logger.info("simple JSON rest ping")
                complete("JSON Service is working (PONG!)")
              } ~
              path("version" / "cluster") {
                logger.info("Version of HBase running on this cluster")
                complete(getClusterVersion)
              } ~
              path("status" / "cluster") {
                logger.info("Cluster status")
                complete(getClusterStatus)
              } ~
              path(Segment / Segment / Segments) { case (table, row, cols) =>
                logger.info(s"Get all the value from columns of a single row, table: $table row: $row cols: $cols")
                complete(getSingleRowColumnValues(table, row, cols))
              } ~
              path(Segment / Segment) { case (table, row) =>
                logger.info(s"Get all values from a single row, table: $table row: $row")
                complete(getSingleRowColumnValues(table, row, List.empty))
              } ~
              path(Segment / "scanner" / IntNumber) { case (table, scannerId) =>
                logger.info("Get the next batch from the scanner")
                complete(s"List the value from Table: $table scanner-id: $scannerId")
              }
          }
      }
}
