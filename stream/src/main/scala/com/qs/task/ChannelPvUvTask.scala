package com.qs.task

import com.qs.`trait`.ProcessData
import com.qs.bean.Message
import com.qs.map.ChannelPvUvFlatMap
import com.qs.reduce.ChannelPvUvReduce
import com.qs.sink.ChannelPvuvSink
import org.apache.flink.streaming.api.scala.DataStream
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.time.Time

object ChannelPvUvTask extends ProcessData {
  /**
   * 数据处理入口
   *
   * @param waterData
   */
  override def process(waterData: DataStream[Message]): Unit = {
    /**
     * 开发步骤：
     * 1.数据转换，新建样例类
     * 2.数据分组
     * 3.划分时间窗口
     * 4.数据聚合
     * 5.数据落地
     */
    waterData.flatMap(new ChannelPvUvFlatMap)
      .keyBy(x => x.getChannelId + x.getTime)
      .timeWindow(Time.seconds(3))
      .reduce(new ChannelPvUvReduce)
      .addSink(new ChannelPvuvSink)


  }
}
