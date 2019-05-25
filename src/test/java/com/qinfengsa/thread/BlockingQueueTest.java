package com.qinfengsa.thread;

import com.qinfengsa.thread.demo.LockDemo;
import com.qinfengsa.thread.queue.BlockingQueue;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

/**
 * {@link com.qinfengsa.thread.queue.BlockingQueue}
 * @author: qinfengsa
 * @date: 2019/5/25 15:54
 */
@Slf4j
public class BlockingQueueTest {


    @Test
    public void queTest() throws InterruptedException {
        BlockingQueue<String> queue = new BlockingQueue(100);
        for(int i=0;i < 10;i++){
            String s = "a" + i;
            int finalI = i;
            new Thread(() -> {
                try {
                    putQueue(queue, finalI,s);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
        Thread.sleep(5000);
        log.debug("last:{}",queue.toString());
    }


    private void putQueue(BlockingQueue<String> queue,int i, String s) throws InterruptedException {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        queue.put(s);
        log.debug("{}:{}",i,queue.toString());
    }

    @Test
    public void test() throws InterruptedException {
        BlockingQueue<String> queue = new BlockingQueue();
        queue.put("A");
        queue.put("B");
        log.debug("1:{}",queue.toString());
        queue.put("C");
        //queue.enqueue("D");
        //queue.enqueue("E");
        //queue.enqueue("F");
        log.debug("1.5:{}",queue.toString());
        queue.take();
        queue.take();
        queue.take();
        log.debug("2:{}",queue.toString());
        queue.put("G");
        queue.put("H");
        queue.put("K");
        log.debug("3:{}",queue.toString());
        log.debug("a:{}",queue.peek());
        log.debug("b:{}",queue.take());
        log.debug("4:{}",queue.toString());
        queue.take();
        queue.take();
        log.debug("4.5:{}",queue.toString());
        queue.take();
        log.debug("5:{}",queue.toString());
    }
}
