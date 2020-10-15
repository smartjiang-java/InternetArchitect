package com.jqk.rocketmq.learn02;


import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;

import java.util.ArrayList;

/**
 * ClassNmae:Producer1
 * Package:com.jqk.rocketmq.producer
 * Description:消息发送者    异步消息
 *
 * @Date:2020/8/31
 * @Author:JQK
 **/

public class Producer2 {

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
        byte[] body = "xxoo的第二条数据".getBytes();
        Message msg = new Message(topic, body);


        //可以设置消息发送失败重投的时间
        //producer.setRetryTimesWhenSendAsyncFailed(1000);

        //发送消息后，不会阻塞去等待broker的确认，采用事件监听的方式去接收broker返回的确认
        //这里也没有broker返回的消息
        producer.send(msg, new SendCallback() {
            public void onSuccess(SendResult sendResult) {
                System.out.println("消息发送成功！");
            }

            public void onException(Throwable e) {
                //如果发生异常，在这里case异常，尝试重投
                e.printStackTrace();
            }
        });

        //异步执行，不要这这里关闭producer，否则还没有收到应答，就已经关闭，会造成异常
        //producer.shutdown();

    }
}
