package com.jqk.rocketmq.learn05;


import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;

/**
 * ClassNmae:Producer1
 * Package:com.jqk.rocketmq.producer
 * Description:消息发送者   过滤消息：给消息增加属性
 *
 * @Date:2020/8/31
 * @Author:JQK
 **/

public class Producer5 {

    public static void main(String[] args) throws Exception {

        DefaultMQProducer producer = new DefaultMQProducer("xxoo");
        producer.setNamesrvAddr("192.168.10.129:9876");

        producer.start();

        String topic = "mytopic001";
        byte[] body = "xxoo的第三条数据".getBytes();

        Message msg = null;
        for (int i = 0; i < 100; i++) {
            msg = new Message(topic, "tagX", "key-JQK", body);

            //给消息增加一条属性
            msg.putUserProperty("age", String.valueOf(i));
        }

        //如果同步发送失败，设置重投次数
        producer.setRetryTimesWhenSendFailed(2);
        //异步发送失败，设置发送次数
        //producer.setRetryTimesWhenSendAsyncFailed(2);
        //是否向其他的broker发送请求，默认是false
        // producer.setRetryAnotherBrokerWhenNotStoreOK(true);

        SendResult send = producer.send(msg);

        //producer.shutdown();

    }
}
