package com.qinfengsa.thread.app2;

import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 消费者
 * @author: qinfengsa
 * @date: 2019/6/9 10:33
 */
@Slf4j
public class Consumer implements Runnable {


    /**
     * 阻塞队列
     */
    private BlockingQueue<String> queue;

    /**
     * 构造函数
     * @param queue
     */
    public Consumer(BlockingQueue<String> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        log.debug("消费者线程启动");
        while (true) {
            try {
                Thread.sleep(10);
                String data = queue.poll(1, TimeUnit.SECONDS);
                if (Objects.nonNull(data)) {
                    log.debug("消费数据:{}", data);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
