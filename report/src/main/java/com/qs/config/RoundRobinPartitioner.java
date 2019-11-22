package com.qs.config;

import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author:qisuhai
 * @date:2019/11/20
 * @description:
 */
public class RoundRobinPartitioner implements Partitioner {

    //定义自增对象数据
    AtomicInteger atomicInteger = new AtomicInteger(0);
    @Override
    public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {

        //获取分区数
        Integer partition = cluster.partitionCountForTopic(topic);
        int i = atomicInteger.incrementAndGet() % partition;
        if (atomicInteger.get() > 1000){
            atomicInteger.set(0);
        }
        return i;

    }

    @Override
    public void close() {

    }

    @Override
    public void configure(Map<String, ?> configs) {

    }
}
