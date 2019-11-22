package com.qs.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.HashMap;

/**
 * @author:qisuhai
 * @date:2019/11/20
 * @description:
 */
@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${kafka.producer.servers}")
    private String servers;
    @Value("${kafka.producer.retries}")
    private String retries;
    @Value("${kafka.producer.batch.size}")
    private String batchSize;
    @Value("${kafka.producer.linger}")
    private String linger;
    @Value("${kafka.producer.buffer.memory}")
    private String memory;

    @Bean
    public KafkaTemplate kafkaTemplate(){
        //kafka生产配置参数
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,servers);
        map.put(ProducerConfig.RETRIES_CONFIG, retries);
        map.put(ProducerConfig.BATCH_SIZE_CONFIG,batchSize);
        map.put(ProducerConfig.LINGER_MS_CONFIG,linger);
        map.put(ProducerConfig.BUFFER_MEMORY_CONFIG,memory);
        //序列化配置
        map.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        map.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        //数据写入分区，实现负载均衡，RoundRobin的轮询算法
        map.put(ProducerConfig.PARTITIONER_CLASS_CONFIG,RoundRobinPartitioner.class);

        //kafka生产对象
        DefaultKafkaProducerFactory<String, String> kafkaProducerFactory = new DefaultKafkaProducerFactory<String, String>(map);
        KafkaTemplate<String, String> kafkaTemplate = new KafkaTemplate<String, String>(kafkaProducerFactory);
        return kafkaTemplate;

    }
}
