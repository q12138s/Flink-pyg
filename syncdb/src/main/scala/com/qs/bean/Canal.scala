package com.qs.bean

import com.alibaba.fastjson.{JSON, JSONObject}

case class Canal (
  columnValueList: String,
  dbName: String,
  emptyCount: String,
  eventType: String,
  logFileName: String,
  logFileOffset: String,
  tableName: String,
  timestamp: String
                 )
object Canal{
  /**
   * 解析json字符串
   */
  def parseJson(str:String):Canal={
    val json: JSONObject = JSON.parseObject(str)
    Canal(
      json.getString("columnValueList"),
      json.getString("dbName"),
      json.getString("emptyCount"),
      json.getString("eventType"),
      json.getString("logFileName"),
      json.getString("logFileOffset"),
      json.getString("tableName"),
      json.getString("timestamp")
    )
  }
}
