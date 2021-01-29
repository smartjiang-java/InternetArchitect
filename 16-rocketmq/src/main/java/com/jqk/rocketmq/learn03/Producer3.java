package com.jqk.rocketmq.learn03;


import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;

/**
 * ClassNmae:Producer1
 * Package:com.jqk.rocketmq.producer
 * Description:消息发送者   单步消息
 *
 * @Date:2020/8/31
 * @Author:JQK
 **/

public class Producer3 {

    public static void main(String[] args) throws Exception {
        //创建生产者
        DefaultMQProducer producer = new DefaultMQProducer("xxoo");
        //设置nameserver地址
        producer.setNamesrvAddr("192.168.10.129:9876");
        //开启消息生产者
        producer.start();

        //topic:消息发送的地址，字符串类型，发给broker
        //body:消息中实际的数据，是一个字节数组，字符串也可以，字节流也可以
        String topic = "mytopic001";
        byte[] body = "xxoo的第三条数据".getBytes();
        Message msg = new Message(topic, body);

        //只发送消息，不保证消息是否安全送达，很有可能丢失消息，比如网络的不确定
        producer.sendOneway(msg);

        //异步执行，不要这这里关闭
        producer.shutdown();

    }
}