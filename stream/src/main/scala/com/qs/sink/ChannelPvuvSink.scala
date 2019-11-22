package com.qs.sink

import com.qs.bean.ChannelPvuv
import com.qs.util.HbaseUtil
import org.apache.commons.lang3.StringUtils
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction

class ChannelPvuvSink extends RichSinkFunction[ChannelPvuv] {
  override def invoke(value: ChannelPvuv): Unit = {
    //设置hbase表参数
    val tableName = "channel"
    val family = "info"
    val pvCol = "pv"
    val uvCol = "uv"
    val rowkey = value.getChannelId + value.getTime
    var pv = value.getPv
    var uv = value.getUv

    //插入数据前，先查询有没有数据
    val pvData: String = HbaseUtil.queryByRowkey(tableName, family, pvCol, rowkey)
    val uvData: String = HbaseUtil.queryByRowkey(tableName, family, uvCol, rowkey)
    if (StringUtils.isNoneBlank(pvData)) {
      pv = pv + pvData.toLong
    }
    if (StringUtils.isNotBlank(uvData)) {
      uv = uv + uvData.toLong
    }

    //封装map
    var map: Map[String, Any] = Map[String,Any]()
    map+=("channelId"->value.getChannelId)
    map+=("time"->value.getTime)
    map+=(pvCol->pv)
    map+=(uvCol->uv)
    HbaseUtil.putMapDataByRowkey(tableName,family,map,rowkey)
  }
}
