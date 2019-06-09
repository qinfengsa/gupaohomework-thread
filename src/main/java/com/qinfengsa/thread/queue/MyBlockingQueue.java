package com.qinfengsa.thread.queue;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 请结合ReentrantLock、Condition实现一个简单的阻塞队列，阻塞队列提供两个方法，一个是put、一个是take
 * 测试类  BlockingQueueTest
 * @author: qinfengsa
 * @date: 2019/5/21 10:42
 */
@Slf4j
public class MyBlockingQueue<E> implements Serializable {

    /**
     * 序列化
     */
    private static final long serialVersionUID = -4725508158285637511L;

    /**
     * 默认打造
     */
    private static final int DEFULT_SIZE = 16;

    /**
     * 默认队列容量
     */
    private final int capacity ;

    /**
     * 队列大小
     */
    private int size = 0;

    /**
     * 队列头结点
     */
    private Node<E> head;

    /**
     * 队列尾结点
     */
    private Node<E> tail;


    /**
     * take 锁
     */
    private final ReentrantLock takeLock = new ReentrantLock();

    /**
     * 当队列元素为空
     */
    private final Condition notEmpty = takeLock.newCondition();


    /**
     * put 锁
     */
    private final ReentrantLock putLock = new ReentrantLock();

    /**
     * 当队列元素为满
     */
    private final Condition notFull = putLock.newCondition();

    /**
     * 构造方法
     * @param capacity 队列容量
     */
    public MyBlockingQueue(int capacity) {
        this.capacity = capacity;
        this.head = new Node<>();
        this.tail = head;
    }

    /**
     * 构造方法
     */
    public MyBlockingQueue() {
        this(DEFULT_SIZE);
    }


    /**
     * 是否为空
     * @return
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * 是否占满
     * @return
     */
    public boolean isFull() {
        return size == capacity;
    }


    /**
     * 数据元素 e 入队
     * @param e
     */
    private void enqueue(E e) {
        // 新建结点放入队尾
        Node node = new Node(e);
        tail.setNext(node);
        tail = node;
        size++;

    }

    /**
     * 唤醒take操作的线程
     * 注：必须和take操作同一把锁，同一个 Condition
     */
    private void signalNotEmpty() {
        takeLock.lock();
        try {
            notEmpty.signal();
        } finally {
            takeLock.unlock();
        }
    }

    /**
     * 唤醒put操作的线程
     * 注：必须和put操作同一把锁，同一个 Condition
     */
    private void signalNotFull() {
        putLock.lock();
        try {
            notFull.signal();
        } finally {
            putLock.unlock();
        }
    }

    /**
     * 入队
     * @param e
     * @throws InterruptedException
     */
    public void put(E e) throws InterruptedException {
        putLock.lock();
        try {
            // 如果队列已满，阻塞
            if (size == capacity){
                notFull.await();
            }
            enqueue(e);
            if (size < capacity) {
                notFull.signal();
            }
        } finally {
            putLock.unlock();
        }
        signalNotEmpty();
    }


    /**
     * 出队
     * @return
     * @throws InterruptedException
     */
    public E take() throws InterruptedException {
        takeLock.lock();
        E result;
        try {
            // 如果队列为空，阻塞
            if (size == 0){
                notEmpty.await();
            }
            result = dequeue();
            if (size > 0) {
                notEmpty.signal();
            }

        } finally {
            takeLock.unlock();
        }
        signalNotFull();
        return result;
    }

    /**
     * 队首元素出队
     * @return
     */
    private E dequeue()  {
        Node node = head.getNext();
        Object result = node.getData();

        head.setNext(node.getNext());
        size--;
        node = null;
        if (size < 1) {
            tail = head;
        }

        return (E)result;
    }

    /**
     * 取队首元素
     * @return
     */
    public Object peek()   {
        /*if (isEmpty()) {
            throw new QueueEmptyException("错误：队列为空");
        }*/
        Node node = head.getNext();
        return node.getData();
    }

    /**
     * toString重写
     * @return
     */
    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("Queue{");


        Node node = this.head.getNext();
        int index = 0;
        while (Objects.nonNull(node)) {
            if (index > 0) {
                sb.append(",");
            }
            sb.append(node.getData());
            node = node.getNext();
            index++;

        }
        sb.append("}");
        sb.append("size=");
        sb.append(size);
        return sb.toString();
    }



    /**
     * 队列元素节点
     * @param <E>
     */
    @Data
    class Node<E> {
        private Object data;

        private Node<E> next;

        public Node(Object data) {
            this.data = data;
        }

        public Node() {
            this(null);
        }


    }

}
