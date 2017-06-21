package de.telefonica.repo

import de.telefonica._
import de.telefonica.services._
import org.apache.hadoop.hbase.client.{ConnectionFactory, Get}
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.{CellUtil, HBaseConfiguration, TableName}

import scala.concurrent.Future


/**
  * Created by dennis kleine on 04.05.17.
  */
class HBaseRepository {
  import scala.concurrent.ExecutionContext.Implicits.global

  def list: Future[Tables] = Future {
    //get the configuration from hbase -> conf/hbase-site.xml
    val conf = HBaseConfiguration.create()
    val connection = ConnectionFactory.createConnection(conf)
    val admin = connection.getAdmin()
    val list = admin.listTables()
    val tables = list.map(_.getNameAsString).toList
    admin.close()
    connection.close()
    Tables(tables)
  }

  def version: Future[Version] = Future {
    //get the configuration from hbase -> conf/hbase-site.xml
    val conf = HBaseConfiguration.create()
    val connection = ConnectionFactory.createConnection(conf)
    val admin = connection.getAdmin()
    val cluster = admin.getClusterStatus()
    val version = cluster.getHBaseVersion
    admin.close()
    connection.close()
    Version(version)
  }

  def status: Future[Server] = Future {
    //get the configuration from hbase -> conf/hbase-site.xml
    val conf = HBaseConfiguration.create()
    val connection = ConnectionFactory.createConnection(conf)
    val admin = connection.getAdmin()
    val cluster = admin.getClusterStatus()
    val status = Server(cluster.getHBaseVersion,
                        cluster.getMaster.toString,
                        cluster.getAverageLoad,
                        cluster.getClusterId,
                        List(cluster.getServers.toString),
                        cluster.getRequestsCount)
    admin.close()
    connection.close()
    status
  }

  def row(tablename: String, rowkey: String, columns: List[String]): Future[Row] = Future {
    //get the configuration from hbase -> conf/hbase-site.xml
    val conf = HBaseConfiguration.create()
    val connection = ConnectionFactory.createConnection(conf)
    val table = connection.getTable(TableName.valueOf(tablename))
    val row = new Get(Bytes.toBytes(rowkey))

    columns.foreach(list => {
      val column = list.split(':')
      val family = column(0)
      val qualifier = column(1)
      row.addColumn(Bytes.toBytes(family), Bytes.toBytes(qualifier))
    })

    val result = table.get(row)
    val cols = result.rawCells()
    val col = cols.map(c => Column(Bytes.toString(CellUtil.cloneFamily(c)),
                                   Bytes.toString(CellUtil.cloneQualifier(c)),
                                   Bytes.toString(CellUtil.cloneValue(c)))).toList

    table.close()
    connection.close()
    Row(rowkey, col)
  }
}
