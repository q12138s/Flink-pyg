package com.qs.task

import com.qs.bean.{Canal, ColPair, HbaseOperation}
import org.apache.flink.streaming.api.scala.DataStream
import org.apache.flink.streaming.api.scala._
import scala.collection.immutable.StringOps
import scala.collection.mutable.ArrayBuffer

object processDataTask {
  /**
   * 将canal bean对象中的数据转换成HbaseOperation
   *
   */
  /**
   * eventType: String,
   * table: String,
   * family: String,
   * colName: String,
   * colValue: String,
   * rowkey: String
   *
   * @param canals
   * @return
   */
  def process(canals: DataStream[Canal])={
      canals.flatMap(line=>{
        val eventType: String = line.eventType
        val tableName: String = line.tableName
        val dbName: String = line.dbName
        val columnValueList: String = line.columnValueList
        //解析columnValueList
        val colPairs: ArrayBuffer[ColPair] = ColPair.parseJsonArr(columnValueList)
        val rowkey: String= colPairs(0).columnValue
        val hbaseTableName: String = dbName+"-"+tableName
        val family = "info"

        eventType match {
          case "INSERT" =>
            colPairs.map(line=>HbaseOperation(eventType,hbaseTableName,family,
              line.columnName,line.columnValue,rowkey))
          case "UPDATE" =>
            colPairs.filter(_.isValid).map(line=>HbaseOperation(eventType,hbaseTableName,family,
              line.columnName,line.columnValue,rowkey))
          case _ =>
            List(HbaseOperation(eventType,hbaseTableName,family,null,null,rowkey))
        }
      })
  }
}
