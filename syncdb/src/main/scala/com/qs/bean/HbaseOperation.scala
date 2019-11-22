package com.qs.bean
/**
 * @Date 2019/11/21
 * @Description hbase操作类，封装hbase的更新数据
 */
case class HbaseOperation(
                           eventType: String,
                           table: String,
                           family: String,
                           colName: String,
                           colValue: String,
                           rowkey: String
                         )
