package com.qs.`trait`

import com.qs.bean.Message
import org.apache.flink.streaming.api.scala.DataStream

trait ProcessData{

  /**
   * 数据处理入口
   * @param waterData
   */
  def process(waterData: DataStream[Message])

}
