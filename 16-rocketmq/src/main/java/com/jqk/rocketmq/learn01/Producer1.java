package com.jqk.rocketmq.learn01;


import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;

import java.util.ArrayList;

/**
 * ClassNmae:Producer1
 * Package:com.jqk.rocketmq.producer
 * Description:消息发送者     同步消息
 *
 * @Date:2020/8/31
 * @Author:JQK
 **/

public class Producer1 {

    public static void main(String[] args) throws Exception {
        //创建生产者
        DefaultMQProducer producer = new DefaultMQProducer("xxoo");
        //设置nameserver地址
        producer.setNamesrvAddr("192.168.10.129:9876");
        //开启消息生产者
        producer.start();

        //topic:消息发送的地址，字符串类型，发给broker
        //body:消息中实际的数据，是一个字节数组，字符串也可以，字节流也可以
        String topic="mytopic001";
        byte[] body="xxoo的第一条数据".getBytes();
        Message msg=new Message(topic,body);

/*        //发送很多条消息，用list接收和传送
        //官方建议，list的消息包不要大于1M
        Message msg=new Message(topic,body);
        Message msg2=new Message(topic,body);
        Message msg3=new Message(topic,body);

        ArrayList<Message> list = new ArrayList<Message>();
        list.add(msg);
        list.add(msg2);
        list.add(msg3);

        SendResult sendResult = producer.send(list);
        */

        //向broker发送数据
        //同步消息发送，发送消息后，等待broker发送收到消息的应答，此时处于阻塞状态
        //返回值为SendResult，broker返回的消息
        SendResult sendResult = producer.send(msg);

        producer.shutdown();

    }
}
