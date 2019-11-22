package com.qs.task

import com.qs.`trait`.ProcessData
import com.qs.bean.{ChannelHot, Message}
import com.qs.sink.ChannelHotSink
import org.apache.flink.streaming.api.scala.DataStream
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.time.Time
object ChannelHotTask extends ProcessData{
  /**
   * 数据处理入口
   *
   * @param waterData
   */
  override def process(waterData: DataStream[Message]): Unit = {
    /**
     * （1）创建样例类，数据转换
     * （2）数据分组
     * （3）划分时间窗口
     * （4）数据聚合
     * （5）数据落地
     */
    waterData.map(line => ChannelHot(line.count,line.userBrowse.channelID))
      .keyBy(_.channelId)
      .timeWindow(Time.seconds(3))
      .reduce((x,y)=>ChannelHot(x.count+y.count,x.channelId))
      .addSink(new ChannelHotSink)
  }
}
