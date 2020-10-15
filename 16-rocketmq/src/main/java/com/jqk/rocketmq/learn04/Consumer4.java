package com.jqk.rocketmq.learn04;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;

import java.util.List;

/**
 * ClassNmae:Consumer1
 * Package:com.jqk.rocketmq.consumer
 * Description:消息消费者
 *
 * @Date:2020/8/31
 * @Author:JQK
 **/

public class Consumer4 {

    public static void main(String[] args) throws Exception {
        //创建消费者
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("xxoocsm");
        //给消费者nameserver的地址
        consumer.setNamesrvAddr("192.168.10.129:9876");


        //每个消费者关注一个topic   topic：关注消息的地址
        // subExpression:过滤器，*表示不过滤，这里改成"tag—X"表示被只接收这种类型的消息
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

        //消息的处理模式，一个group里面消息模式保持一致，否则会乱的
        // 广播模式，可以被多个消费者接收，消费状态由consumer维护，没有重投机制
        //consumer.setMessageModel(MessageModel.BROADCASTING);
        //默认的集群模式，重投时不保证是同一个消费者，消费状态由broker维护
        consumer.setMessageModel(MessageModel.CLUSTERING);

        //启动消费者
        consumer.start();
    }
}
