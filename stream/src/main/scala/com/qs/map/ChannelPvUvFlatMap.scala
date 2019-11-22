package com.qs.map

import com.qs.bean.{ChannelPvuv, Message, UserBrowse, UserState}
import com.qs.util.TimeUtil
import org.apache.flink.api.common.functions.RichFlatMapFunction
import org.apache.flink.util.Collector

class ChannelPvUvFlatMap extends RichFlatMapFunction[Message, ChannelPvuv] {
  //定义日期模板
  val hour = "yyyyMMddHH"
  val day = "yyyyMMdd"
  val month = "yyyyMM"

  override def flatMap(value: Message, out: Collector[ChannelPvuv]): Unit = {

    val userBrowse: UserBrowse = value.userBrowse
    val userID: Long = userBrowse.userID
    val categoryID: Long = userBrowse.categoryID
    val timestamp: Long = userBrowse.timestamp

    //根据用户id和事件事件获取用户的访问状态
    val userState: UserState = UserState.getUserState(userID, timestamp)
    val isNew: Boolean = userState.isNew
    val firstHour: Boolean = userState.isFirstHour
    val firstDay: Boolean = userState.isFirstDay
    val firstMonth: Boolean = userState.isFirstMonth
    //日期格式化
    val hourTime: String = TimeUtil.parseTime(timestamp, hour)
    val dayTime: String = TimeUtil.parseTime(timestamp, day)
    val monthTime: String = TimeUtil.parseTime(timestamp, month)
    //创建pvuv对象，封装数据
    val pvuv = new ChannelPvuv
    pvuv.setChannelId(categoryID)
    pvuv.setPv(1)

    //根据用户的访问状态封装数据
    if (isNew == true) {
      pvuv.setUv(1)
    }

    //时
    if (firstHour == true) {
      pvuv.setUv(1)
    } else {
      pvuv.setUv(0)
    }
    pvuv.setTime(hourTime)
    out.collect(pvuv)
    //天维度
    firstDay match {
      case true =>
        pvuv.setUv(1)
      case false =>
        pvuv.setUv(0)
    }
    pvuv.setTime(dayTime)
    out.collect(pvuv)

    //月维度
    firstMonth match {
      case true =>
        pvuv.setUv(1)
      case false =>
        pvuv.setUv(0)
    }
    pvuv.setTime(monthTime)
    out.collect(pvuv)
  }
}
