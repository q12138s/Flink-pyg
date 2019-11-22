package com.qs.util

import java.util.Date

import org.apache.commons.lang.time.FastDateFormat

object TimeUtil {
  /**
   *
   * @param timestamp   格式化时间
   * @param timeFormat   格式化模板
   * @return
   */
  def parseTime(timestamp:Long,timeFormat:String):String={
    val dateFormat: FastDateFormat = FastDateFormat.getInstance(timeFormat)
    val date = new Date(timestamp)
    val str: String = dateFormat.format(date)
    str
  }

}
