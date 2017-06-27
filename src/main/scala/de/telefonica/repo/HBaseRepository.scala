package de.telefonica.repo


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

  def filter(tablename: String, rowkeys: List[String], columns: List[String]): Future[List[Row]] = Future {

    //get the configuration from hbase -> conf/hbase-site.xml
    val conf = HBaseConfiguration.create()
    val connection = ConnectionFactory.createConnection(conf)
    val table = connection.getTable(TableName.valueOf(tablename))

    import scala.collection.JavaConverters._
    val rows: java.util.List[Get] = rowkeys.map(r => new Get(Bytes.toBytes(r))).asJava  //convert Scala List to Java List for HBase BulkGet
    val result = table.get(rows)  //HBase BulkGet
    val colsArr = result.map(c => c.rawCells())


    val ret = colsArr.map(cellArr => {
      val listCols = cellArr.map(c => Column(Bytes.toString(CellUtil.cloneFamily(c)), Bytes.toString(CellUtil.cloneQualifier(c)), Bytes.toString(CellUtil.cloneValue(c)))).toList
      val k = rowkeys.head //Bytes.toString(CellUtil.cloneRow(k))
      Row(k, listCols)
      }
    ).toList

    table.close()
    connection.close()
    ret


//      List(
//        Row("test1", List(Column("test1","test1","test1"),Column("test2","test2","test2"), Column("test3","test3","test3"))),
//        Row("test2", List(Column("test1","test1","test1"),Column("test2","test2","test2"), Column("test3","test3","test3"))),
//        Row("test3", List(Column("test1","test1","test1"),Column("test2","test2","test2"), Column("test3","test3","test3")))
//      )

  }
}
