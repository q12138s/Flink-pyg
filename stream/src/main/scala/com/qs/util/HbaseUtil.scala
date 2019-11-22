package com.qs.util



import com.qs.config.GlobalConfig
import org.apache.flink.runtime.executiongraph.Execution
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.{HBaseConfiguration, HColumnDescriptor, HTableDescriptor, TableName}
import org.apache.hadoop.hbase.client._
object HbaseUtil {

    //获取配置对象
    private val configuration: Configuration =
    HBaseConfiguration.create()
    configuration.set("hbase.zookeeper.quorum", GlobalConfig.hbaseZk)
    configuration.set("hbase.rpc.timeout", GlobalConfig.hbaseRpc)
    configuration.set("hbase.client.operation.timeout", GlobalConfig.hbaseOpe)
    configuration.set("hbase.client.scanner.timeout.period", GlobalConfig.hbaseScan)
    //获取hbase连接对象
    val connection: Connection = ConnectionFactory.createConnection(configuration)
    //获取客户端集群实例对象
    private val admin: Admin =
    connection.getAdmin

    /**
     * 建表
     */
    def initTable(tableName: String, family: String): Table = {

      val hbaseTableName: TableName = TableName.valueOf(tableName)
      if (!admin.tableExists(hbaseTableName)) {

        //获取表描述器
        val tableNameDescriptor = new HTableDescriptor(hbaseTableName)
        //获取列族描述器
        val columnDescriptor = new HColumnDescriptor(family)
        tableNameDescriptor.addFamily(columnDescriptor)
        //建表
        admin.createTable(tableNameDescriptor)
      }

      //如果表存在，直接获取
      val table: Table = connection.getTable(hbaseTableName)
      table
    }

    /**
     * 查询
     * 根据rowkey查询指定列的数据
     */
    def queryByRowkey(tableName: String, family: String, colName: String, rowkey: String): String = {

      //初始化表
      val table: Table = initTable(tableName, family)
      var str = ""
      try {
        val get = new Get(rowkey.getBytes())
        val result: Result = table.get(get)
        val bytes: Array[Byte] = result.getValue(family.getBytes(), colName.getBytes())
        if (bytes != null && bytes.length > 0) {
          str = new String(bytes)
        }
      } catch {
        case e: Exception => e.printStackTrace()
      } finally {
        table.close()
      }
      str
    }

    /**
     * (1)插入
     * 根据rowkey，插入单列数据
     */
    def putDataByRowkey(tableName: String, family: String, colName: String, colValue: String, rowkey: String): Unit = {

      //初始化表
      val table: Table = initTable(tableName, family)
      try {
        val put = new Put(rowkey.getBytes())
        put.addColumn(family.getBytes(), colName.getBytes(), colValue.getBytes())
        table.put(put)
      } catch {
        case e: Exception => e.printStackTrace()
      } finally {
        table.close()
      }
    }

    /**
     * (2)插入
     * 根据rowkey，插入多列数据
     */
    def putMapDataByRowkey(tableName: String, family: String, map: Map[String, Any], rowkey: String): Unit = {

      //初始化表
      val table: Table = initTable(tableName, family)
      try {
        val put = new Put(rowkey.getBytes())
        //循环map，key作为列名，value作为列值
        for ((x, y) <- map) {
          put.addColumn(family.getBytes(), x.getBytes(), y.toString.getBytes())
        }
        table.put(put)
      } catch {
        case e: Exception => e.printStackTrace()
      } finally {
        table.close()
      }
    }

    /**
     * 删除
     * 根据rowkey删除指定列族数据
     */
    def delByRowkey(tableName: String, family: String, rowkey: String): Unit = {
      //初始化表
      val table: Table = initTable(tableName, family)
      try {
        val delete = new Delete(rowkey.getBytes())
        delete.addFamily(family.getBytes())
        table.delete(delete)
      } catch {
        case e: Exception => e.printStackTrace()
      } finally {
        table.close()
      }
    }

    def main(args: Array[String]): Unit = {

      //    //插入单列数据
//      putDataByRowkey("test-1120", "info", "k1", "11", "001")
      //    //    //查询单列数据
      val str: String = queryByRowkey("test-1120", "info", "k1", "001")
          println(str)
      //插入多列数据
      //    putMapDataByRowkey("test-1120","info",Map("k2"->"22","k3"->"33"),"002")

      //删除数据
      //    delByRowkey("test-1120","info","001")

    }
  }


