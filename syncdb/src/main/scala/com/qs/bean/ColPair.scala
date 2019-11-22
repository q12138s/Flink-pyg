package com.qs.bean

import com.alibaba.fastjson.{JSON, JSONArray, JSONObject}

import scala.collection.mutable.ArrayBuffer

case class ColPair(
                    columnName: String,
                    columnValue: String,
                    isValid: Boolean
                  )
object ColPair{
  def parseJsonArr(str:String)={
    val arrayBuffer = new ArrayBuffer[ColPair]()
    val jsonArr: JSONArray = JSON.parseArray(str)
    for (line <- 0 until jsonArr.size()) {
      val json: JSONObject = jsonArr.getJSONObject(line)
      arrayBuffer+=ColPair(
        json.getString("columnName"),
        json.getString("columnValue"),
        json.getBoolean("isValid")
      )

    }
    arrayBuffer
  }
}