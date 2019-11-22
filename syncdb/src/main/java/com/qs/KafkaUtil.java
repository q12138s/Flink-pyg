package com.qs;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;
/**
 * @author:qisuhai
 * @date:2019/11/21
 * @description:
 */
public class KafkaUtil {


    /**
     * 获取kafka生产对象
     * Producer
     */
    public static Producer producer(){
        Properties props = new Properties();
        props.put("bootstrap.servers", "hadoop01:9092,hadoop02:9092,hadoop03:9092");
        props.put("acks", "1");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        Producer<String, String> producer = new KafkaProducer<String,String>(props);
        return producer;
    }

    /**
     * 发送数据
     */
    public static void sendData(String topic ,String value){
        //获取kafka生产对象
        Producer producer = producer();
        //发送数据到kafka
        producer.send(new ProducerRecord(topic,value));
    }

}
