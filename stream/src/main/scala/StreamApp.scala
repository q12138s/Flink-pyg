import java.util.Properties

import com.alibaba.fastjson.{JSON, JSONObject}
import com.qs.bean.{ChannelHot, Message, UserBrowse}
import com.qs.config.GlobalConfig
import com.qs.task.{ChannelHotTask, ChannelPvUvTask}
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.environment.CheckpointConfig
import org.apache.flink.streaming.api.functions.AssignerWithPeriodicWatermarks
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.watermark.Watermark
object StreamApp {
  /**
   * 启动类
   * @param args
   */
  def main(args: Array[String]): Unit = {

    /**
     * 步骤：
     * 1.获取流处理执行环境
     * 2.设置事件时间
     * 3.设置检查点机制
     * 4.配置kafka,加载kafka数据源
     * 5.数据源转换bean
     * 6.设置水位线
     * 7.子任务实现
     *   （1）创建样例类，数据转换
     *   （2）数据分组
     *   （3）划分时间窗口
     *   （4）数据聚合
     *   （5）数据落地
     * 8.触发执行
     */

    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
    //设置检查点机制
    env.enableCheckpointing(5000)
    env.getCheckpointConfig.setCheckpointTimeout(60000)
    env.getCheckpointConfig.setFailOnCheckpointingErrors(false)
    env.getCheckpointConfig.setMaxConcurrentCheckpoints(1)
    env.getCheckpointConfig.enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)
    //配置kafka，加载kafka数据源
    val properties = new Properties()
    properties.setProperty("bootstrap.servers",GlobalConfig.kafkaServer)
    properties.setProperty("group.id",GlobalConfig.kafkaGroupId)
    val kafkaConsumer = new FlinkKafkaConsumer011[String](GlobalConfig.kafkaTopic,new SimpleStringSchema(),properties)
    //设置从头消费
    kafkaConsumer.setStartFromEarliest()
    //加载kafka数据源
    val kafkaSource: DataStream[String] = env.addSource(kafkaConsumer)

    //数据源转换成bean
    val messages: DataStream[Message] = kafkaSource.map(line => {
      val json: JSONObject = JSON.parseObject(line)
      val count: Int = json.getIntValue("count")
      val str: String = json.getString("str")
      val timestamp: Long = json.getLongValue("timestamp")
      val browse: UserBrowse = UserBrowse.parseLine(str)
      Message(count, timestamp, browse)
    })

    //6.设置水位线
    val waterData: DataStream[Message] = messages.assignTimestampsAndWatermarks(new AssignerWithPeriodicWatermarks[Message] {
      val delayTime: Long = 2000L
      var currentTimestamp: Long = 0l

      override def getCurrentWatermark: Watermark = {
        new Watermark(currentTimestamp - delayTime)
      }

      override def extractTimestamp(element: Message, previousElementTimestamp: Long): Long = {
        val timestamp: Long = element.userBrowse.timestamp
        currentTimestamp = Math.max(currentTimestamp, timestamp)
        timestamp
      }
    })

    /**
     * 7.子任务实现
     * *   （1）创建样例类，数据转换
     * *   （2）数据分组
     * *   （3）划分时间窗口
     * *   （4）数据聚合
     * *   （5）数据落地
     */
    //业务一：实时频道的热点统计
//    ChannelHotTask.process(waterData)
    ChannelPvUvTask.process(waterData)

    env.execute()

  }

}
