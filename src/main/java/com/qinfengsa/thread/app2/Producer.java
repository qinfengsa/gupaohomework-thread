package com.qinfengsa.thread.app2;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 生产者
 * @author: qinfengsa
 * @date: 2019/6/9 10:33
 */
@Slf4j
public class Producer implements Runnable {
    /**
     * 阻塞队列
     */
    private BlockingQueue<String> queue;

    private AtomicInteger count = new AtomicInteger(0);

    /**
     * 构造函数
     * @param queue
     */
    public Producer(BlockingQueue<String> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        log.debug("生产者线程启动");

        while (true) {
            try {
                String data = Thread.currentThread().getName() + "生产数据：" + count.getAndIncrement();
                Thread.sleep(800);
                log.debug("将数据{}放入队列",data);
                // 设定超时时间
                if (! queue.offer(data, 1, TimeUnit.SECONDS)) {
                    log.error("放入数据失败：{}" , data);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
