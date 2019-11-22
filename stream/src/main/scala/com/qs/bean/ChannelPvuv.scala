package com.qs.bean

import scala.beans.BeanProperty

class ChannelPvuv {
  @BeanProperty var channelId:Long =0L
  @BeanProperty var time:String =null
  @BeanProperty var pv:Long = 0L
  @BeanProperty var uv:Long = 0L
}
