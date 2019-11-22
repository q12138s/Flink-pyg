package com.qs.config

import com.typesafe.config.{Config, ConfigFactory}

object GlobalConfig {


    //管理配置类，直接读取配置文件中变量
    val config: Config = ConfigFactory.load()
    //kafka配置
    val kafkaServer: String = config.getString("bootstrap.servers")
    val kafkaTopic: String = config.getString("input.topic")
    val kafkaGroupId: String = config.getString("group.id")


}
