package com.qinfengsa.thread.app2;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 生产者消费者模型
 * @author: qinfengsa
 * @date: 2019/6/9 17:21
 */
public class ProducerTest {

    public static void main(String[] args) throws InterruptedException {
        BlockingQueue<String> queue = new ArrayBlockingQueue<>(10);
        // 生产者
        Producer producer1 = new Producer(queue);
        Producer producer2 = new Producer(queue);
        Producer producer3 = new Producer(queue);
        // 消费者
        Consumer consumer = new Consumer(queue);
        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        ThreadPoolExecutor executor = new ThreadPoolExecutor(4,4 ,60 ,
                TimeUnit.SECONDS, new LinkedBlockingQueue<>(),threadFactory);
        executor.execute(producer1);
        executor.execute(producer2);
        executor.execute(producer3);
        executor.execute(consumer);
        Thread.sleep(5000);
    }
}
