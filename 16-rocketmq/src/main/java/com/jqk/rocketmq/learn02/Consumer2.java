package com.jqk.rocketmq.learn02;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.List;

/**
 * ClassNmae:Consumer1
 * Package:com.jqk.rocketmq.consumer
 * Description:消息消费者
 *
 * @Date:2020/8/31
 * @Author:JQK
 **/

public class Consumer2 {

    public static void main(String[] args) throws Exception {
        //创建消费者
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("xxoocsm");
        //给消费者nameserver的地址
        consumer.setNamesrvAddr("192.168.10.129:9876");
        //每个消费者关注一个topic   topic：关注消息的地址
        // subExpression:过滤器，*表示不过滤
        consumer.subscribe("mytopic001", "*");
        //通过监听器接收消息
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
                for (Message msg : list) {
                    System.out.println(new String(msg.getBody()));
                }
                //默认情况下，这条消息只能被一个消费者拿到，一对一的
                //message 状态修改
                //ack
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        //启动消费者
        consumer.start();
    }
}
