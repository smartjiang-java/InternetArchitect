# Disruptor

���ߣ���ʿ�� http://www.mashibing.com

������£�2019��10��22��

## ����

��ҳ��http://lmax-exchange.github.io/disruptor/

Դ�룺https://github.com/LMAX-Exchange/disruptor

GettingStarted: https://github.com/LMAX-Exchange/disruptor/wiki/Getting-Started

api: http://lmax-exchange.github.io/disruptor/docs/index.html

maven: https://mvnrepository.com/artifact/com.lmax/disruptor

## Disruptor���ص�

�Ա�ConcurrentLinkedQueue : ����ʵ��

JDK��û��ConcurrentArrayQueue

Disruptor������ʵ�ֵ�

�������߲�����ʹ�û���Buffer��ֱ�Ӹ��ǣ�����������ɵ����ݣ�����GCƵ��

ʵ���˻����¼���������������ģʽ���۲���ģʽ��

## RingBuffer

���ζ���

RingBuffer����ţ�ָ����һ�����õ�Ԫ��

��������ʵ�֣�û����βָ��

�Ա�ConcurrentLinkedQueue��������ʵ�ֵ��ٶȸ���

> ���糤��Ϊ8������ӵ���12��Ԫ�ص�ʱ�����ĸ�������أ���12%8����
>
> ��Buffer��������ʱ�򵽵��Ǹ��ǻ��ǵȴ�����Producer����
>
> ������Ϊ2��n���ݣ����ڶ����Ƽ��㣬���磺12%8 = 12 & (8 - 1)  pos = num & (size -1)

## Disruptor��������

1. ����Event - ��������Ҫ�����Ԫ��

2. ����Event����������������

   > ����ǣ����Ч�����⣺disruptor��ʼ����ʱ�򣬻����Event��������ringBuffer�����ڴ����ǰ����
   >
   > GC��Ƶ�ʻή��

3. ����EventHandler�������ߣ������������е�Ԫ��

## �¼�����ģ��

```java
long sequence = ringBuffer.next();  // Grab the next sequence
try {
    LongEvent event = ringBuffer.get(sequence); // Get the entry in the Disruptor
    // for the sequence
    event.set(8888L);  // Fill with data
} finally {
    ringBuffer.publish(sequence);
}
```

## ʹ��EventTranslator�����¼�

```java
//===============================================================
        EventTranslator<LongEvent> translator1 = new EventTranslator<LongEvent>() {
            @Override
            public void translateTo(LongEvent event, long sequence) {
                event.set(8888L);
            }
        };

        ringBuffer.publishEvent(translator1);

        //===============================================================
        EventTranslatorOneArg<LongEvent, Long> translator2 = new EventTranslatorOneArg<LongEvent, Long>() {
            @Override
            public void translateTo(LongEvent event, long sequence, Long l) {
                event.set(l);
            }
        };

        ringBuffer.publishEvent(translator2, 7777L);

        //===============================================================
        EventTranslatorTwoArg<LongEvent, Long, Long> translator3 = new EventTranslatorTwoArg<LongEvent, Long, Long>() {
            @Override
            public void translateTo(LongEvent event, long sequence, Long l1, Long l2) {
                event.set(l1 + l2);
            }
        };

        ringBuffer.publishEvent(translator3, 10000L, 10000L);

        //===============================================================
        EventTranslatorThreeArg<LongEvent, Long, Long, Long> translator4 = new EventTranslatorThreeArg<LongEvent, Long, Long, Long>() {
            @Override
            public void translateTo(LongEvent event, long sequence, Long l1, Long l2, Long l3) {
                event.set(l1 + l2 + l3);
            }
        };

        ringBuffer.publishEvent(translator4, 10000L, 10000L, 1000L);

        //===============================================================
        EventTranslatorVararg<LongEvent> translator5 = new EventTranslatorVararg<LongEvent>() {

            @Override
            public void translateTo(LongEvent event, long sequence, Object... objects) {
                long result = 0;
                for(Object o : objects) {
                    long l = (Long)o;
                    result += l;
                }
                event.set(result);
            }
        };

        ringBuffer.publishEvent(translator5, 10000L, 10000L, 10000L, 10000L);
```

## ʹ��Lamda���ʽ

```java
package com.mashibing.disruptor;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.util.DaemonThreadFactory;

public class Main03
{
    public static void main(String[] args) throws Exception
    {
        // Specify the size of the ring buffer, must be power of 2.
        int bufferSize = 1024;

        // Construct the Disruptor
        Disruptor<LongEvent> disruptor = new Disruptor<>(LongEvent::new, bufferSize, DaemonThreadFactory.INSTANCE);

        // Connect the handler
        disruptor.handleEventsWith((event, sequence, endOfBatch) -> System.out.println("Event: " + event));

        // Start the Disruptor, starts all threads running
        disruptor.start();

        // Get the ring buffer from the Disruptor to be used for publishing.
        RingBuffer<LongEvent> ringBuffer = disruptor.getRingBuffer();


        ringBuffer.publishEvent((event, sequence) -> event.set(10000L));

        System.in.read();
    }
}
```

## ProducerType�������߳�ģʽ

> ProducerType������ģʽ Producer.MULTI��Producer.SINGLE
>
> Ĭ����MULTI����ʾ�ڶ��߳�ģʽ�²���sequence
>
> ���ȷ���ǵ��߳������ߣ���ô����ָ��SINGLE��Ч�ʻ�����
>
> ����Ƕ�������ߣ����̣߳�����ģʽָ��ΪSINGLE�����ʲô�����أ�

## �ȴ�����

1��(���ã�BlockingWaitStrategy��ͨ���߳������ķ�ʽ���ȴ������߻��ѣ������Ѻ���ѭ�����������sequence�Ƿ��Ѿ����ѡ�

2��BusySpinWaitStrategy���߳�һֱ�����ȴ������ܱȽϺ�cpu

3��LiteBlockingWaitStrategy���߳������ȴ������߻��ѣ���BlockingWaitStrategy��ȣ�������signalNeeded.getAndSet,��������߳�ͬʱ����һ������waitfor,һ������signalAllʱ�����Լ���lock��������.

4��LiteTimeoutBlockingWaitStrategy����LiteBlockingWaitStrategy��ȣ�����������ʱ�䣬����ʱ������쳣��

5��PhasedBackoffWaitStrategy������ʱ������ʹ���ĵȴ�����������ʹ�����ֵȴ�����

6��TimeoutBlockingWaitStrategy�������BlockingWaitStrategy��˵�������˵ȴ�ʱ�䣬���������쳣

7�������ã�YieldingWaitStrategy������100�Σ�Ȼ��Thread.yield()�ó�cpu

8. �����ã�SleepingWaitStrategy : sleep

## �������쳣����

Ĭ�ϣ�disruptor.setDefaultExceptionHandler()

���ǣ�disruptor.handleExceptionFor().with()

## ��������

