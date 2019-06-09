package com.qinfengsa.thread;

import com.qinfengsa.thread.queue.MyBlockingQueue;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;

/**
 * @author: qinfengsa
 * @date: 2019/5/25 16:38
 */
@Slf4j
public class BlockingDemo {

    static volatile boolean isFlag = false;

    MyBlockingQueue<String> ab = new MyBlockingQueue(10);// 阻塞队列
    /*{
        init(); //构造块初始化
    }*/

    public void init(){

        new Thread(()->{
            while(true) {
                try {
                    String data = ab.take(); // 阻塞方式获得元素
                    log.debug("take:{}" ,data);
                } catch (InterruptedException e) {
                    log.error(e.getMessage(),e);
                }
            }
        }).start();
    }

    public void addData(String data) throws InterruptedException {
        ab.put(data);
        log.debug("put:{}",data);

    }

    public static void main(String[] args) throws InterruptedException {
        BlockingDemo blockingDemo = new BlockingDemo();

        for(int i=0; i < 50;i++){
            int f = i;
            new Thread(() -> {
                try {
                    Thread.sleep(100);
                    blockingDemo.addData("data-"+f);;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
        Thread.sleep(1000);
        log.debug("take开始");
        blockingDemo.init();
    }
}
