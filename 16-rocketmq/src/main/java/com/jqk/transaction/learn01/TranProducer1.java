package com.jqk.transaction.learn01;


import org.apache.rocketmq.client.producer.*;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;

/**
 * ClassNmae:Producer1
 * Package:com.jqk.rocketmq.producer
 * Description:消息发送者     同步消息
 *
 * @Date:2020/8/31
 * @Author:JQK
 **/

public class TranProducer1 {

    public static void main(String[] args) throws Exception {

        //这里用TransactionMQProducer
        TransactionMQProducer producer = new TransactionMQProducer("xxoo");
        producer.setNamesrvAddr("192.168.10.129:9876");


        //回调
        producer.setTransactionListener(new TransactionListener() {
            public LocalTransactionState executeLocalTransaction(Message message, Object o) {
                //执行本地事务,一般是在这里进行try-catch

                System.out.println("========executeLocalTransaction===================");
                System.out.println("message" + new String(message.getBody()));
                //获取事务Id
                System.out.println("message" + message.getTransactionId());

                /**
                 * a()
                 * b()
                 * c()
                 */

                //本地事务执行成功，提交消息
                // return LocalTransactionState.COMMIT_MESSAGE;
                //不成功，回滚消息
                //return  LocalTransactionState.ROLLBACK_MESSAGE;
                //不知道,此时Broker会执行回调
                return LocalTransactionState.UNKNOW;
            }

            public LocalTransactionState checkLocalTransaction(MessageExt messageExt) {
                //BroKer端回调，检查事务


                System.out.println("========checkLocalTransaction===================");
                System.out.println("message" + new String(messageExt.getBody()));
                //获取事务Id
                System.out.println("message" + messageExt.getTransactionId());

                return LocalTransactionState.COMMIT_MESSAGE;
                //假如返回的是unkon，会隔一段时间进行回调一次
                // return LocalTransactionState.UNKNOW;
                // return LocalTransactionState.ROLLBACK_MESSAGE;
            }
        });


        producer.start();

        String topic = "mytopic001";
        byte[] body = "xxoo的第一条数据".getBytes();
        Message msg = new Message(topic, body);

        //后面是个动态参数，暂时先不传
        TransactionSendResult sendResult = producer.sendMessageInTransaction(msg, null);
        System.out.println("sendResult" + sendResult);

        //producer.shutdown();

    }
}
