package com.qs

import java.util.Properties

import com.alibaba.fastjson.JSON
import com.qs.bean.{Canal, HbaseOperation}
import com.qs.config.GlobalConfig
import com.qs.task.processDataTask
import com.qs.util.HbaseUtil
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.environment.CheckpointConfig
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011
import org.apache.flink.streaming.api.scala._
object SyncApp {

  def main(args: Array[String]): Unit = {
    /**
     * 步骤：
     * 1.获取流处理执行环境
     * 2.设置检查点机制
     * 3.配置kafka,加载kafka数据源
     * 4.数据源转换bean
     * 5.数据处理（落地）
     * 6.触发执行
     */

    //1.获取流处理执行环境
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

    //2.设置检查点机制
    env.enableCheckpointing(5000)
    env.getCheckpointConfig.setCheckpointTimeout(60000)
    env.getCheckpointConfig.setFailOnCheckpointingErrors(false)
    env.getCheckpointConfig.setMaxConcurrentCheckpoints(1)
    env.getCheckpointConfig.enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)
    //配置kafka
    val properties = new Properties()
    properties.setProperty("bootstrap.servers",GlobalConfig.kafkaServer)
    val kafkaConsumer = new FlinkKafkaConsumer011[String](GlobalConfig.kafkaTopic,new SimpleStringSchema(),properties)
    //加载kafka数据源
    val kafkaSource: DataStream[String] = env.addSource(kafkaConsumer)
    //数据源转换bean
    val canals: DataStream[Canal] = kafkaSource.map(line => {
      val canal: Canal = Canal.parseJson(line)
      canal
    })
    //将bean转成Hbase需要的参数对象
    val hbaseOperations: DataStream[HbaseOperation] = processDataTask.process(canals)
    //数据处理
    hbaseOperations.map(line=>{
      line.eventType match{
        case "DELETE"=>
          HbaseUtil.delByRowkey(line.table,line.family,line.rowkey)
        case _ =>
          HbaseUtil.putDataByRowkey(line.table,line.family,line.colName,line.colValue,line.rowkey)

      }
    })
    env.execute()
  }

}
