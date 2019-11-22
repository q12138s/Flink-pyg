package com.qs.reduce

import com.qs.bean.ChannelPvuv
import org.apache.flink.api.common.functions.ReduceFunction

class ChannelPvUvReduce extends ReduceFunction[ChannelPvuv]{
  /**
   * 数据聚合
   * @param value1
   * @param value2
   * @return
   */
  override def reduce(value1: ChannelPvuv, value2: ChannelPvuv): ChannelPvuv = {
    val pvuv = new ChannelPvuv
    pvuv.setChannelId(value1.getChannelId)
    pvuv.setTime(value1.getTime)
    pvuv.setPv(value1.getPv+value2.getPv)
    pvuv.setUv(value1.getUv+value2.getUv)
    pvuv
  }
}
