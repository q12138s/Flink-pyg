package com.qs.bean

import com.qs.util.{HbaseUtil, TimeUtil}
import org.apache.commons.lang3.StringUtils

case class UserState(
                      isNew: Boolean = false, //是否是新用户
                      isFirstHour: Boolean = false, //是否是小时内的首次访问
                      isFirstDay: Boolean = false, //是否是天以内的首次访问
                      isFirstMonth: Boolean = false //是否是月以内的首次访问

                    )

object UserState {
  //设置模板格式
  val hour = "yyyyMMddHH"
  val day = "yyyyMMdd"
  val month = "yyyyMM"

  def getUserState(userId: Long, timestamp: Long):UserState= {
    //定义hbase表
    val tableName = "userState"
    val family = "info"
    val firstVisitCol = "firstVisitTime"
    val lastVisitCol = "lastVisitTime"
    val rowkey = userId.toString
    //初始化状态
    var isNew: Boolean = false //是否是新用户
    var isFirstHour: Boolean = false //是否是小时内的首次访问
    var isFirstDay: Boolean = false //是否是天以内的首次访问
    var isFirstMonth: Boolean = false //是否是月以内的首次访问

    //查询hbase首次访问列，如果首次访问时间为null,说明是第一次
    val firstVisitTimeData: String = HbaseUtil.queryByRowkey(tableName, family, firstVisitCol, rowkey)
    if (StringUtils.isEmpty(firstVisitTimeData)) {
      //此次为首次访问
      HbaseUtil.putDataByRowkey(tableName, family, firstVisitCol, timestamp.toString, rowkey)
      HbaseUtil.putDataByRowkey(tableName, family, lastVisitCol, timestamp.toString, rowkey)
      isNew = true
      isFirstHour = true
      isFirstDay = true
      isFirstMonth = true
      //收集状态
      UserState(isNew, isFirstHour, isFirstDay, isFirstMonth)
    } else {
      //不是首次访问，判断 时间 维度
      //查询最后访问时间
      val lastVisitTimeData: String = HbaseUtil.queryByRowkey(tableName, family, lastVisitCol, rowkey)
      //时
      if (TimeUtil.parseTime(timestamp, hour).toLong > TimeUtil.parseTime(lastVisitTimeData.toLong, hour).toLong) {
        isFirstHour = true
      }
      //天
      if (TimeUtil.parseTime(timestamp, day).toLong > TimeUtil.parseTime(lastVisitTimeData.toLong, day).toLong) {
        isFirstDay = true
      }

      //月
      if (TimeUtil.parseTime(timestamp, month).toLong > TimeUtil.parseTime(lastVisitTimeData.toLong, month).toLong) {
        isFirstMonth = true
      }

      //更新最近一次访问的时间
      HbaseUtil.putDataByRowkey(tableName,family,lastVisitCol,timestamp.toString,rowkey)
      UserState(isNew, isFirstHour, isFirstDay, isFirstMonth)
    }

  }


}