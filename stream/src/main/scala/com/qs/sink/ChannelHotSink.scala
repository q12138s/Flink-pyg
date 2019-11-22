package com.qs.sink

import com.qs.bean.ChannelHot
import com.qs.util.HbaseUtil
import org.apache.commons.lang3.StringUtils
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction


class ChannelHotSink extends RichSinkFunction[ChannelHot]{
  override def invoke(value: ChannelHot): Unit = {
    val tableName="channel"
    val family="info"
    val colName="count"
    val rowKey = value.channelId.toString
    var count=value.count
      val countData: String = HbaseUtil.queryByRowkey(tableName,family,colName,rowKey)
    if (StringUtils.isNotBlank(countData)){
      count = count +countData.toInt

    }
    HbaseUtil.putDataByRowkey(tableName,family,colName,count.toString,rowKey)
  }
}
