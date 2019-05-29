ReentrantReadWriteLock在初始化时会创建读锁和写锁;  
同时会根据参数创建FairSync或者NonfairSync，两者都继承了内部类Sync,本质是一个同步阻塞队列  
需要明确：读锁是共享锁，读线程在同一时刻可以允许多个线程访问;   
写锁是独占锁,当线程获得写锁后,任何的读写操作都会被阻塞  

## WriteLock  
1、首先要去获得锁独占tryAcquire 
```Java
class WriteLock {
    protected final boolean tryAcquire(int acquires) {
        // 获取当前线程
        Thread current = Thread.currentThread();
        int c = getState();
        int w = exclusiveCount(c);// 写锁state 二进制后16位
        if (c != 0) {// 已经有线程获得了锁 
            // 不存在写锁 且 c > 0,说明写锁（共享锁）状态不为0,有线程读取数据,
            // 不能进行写操作，或出现脏数据
            // 或者 当前线程 <> 获得锁的线程
            if (w == 0 || current != getExclusiveOwnerThread())
                return false;
            if (w + exclusiveCount(acquires) > MAX_COUNT)
                throw new Error("Maximum lock count exceeded");
            // 修改线程状态 独占锁重入次数+1
            setState(c + acquires);
            return true;
        }
        // c == 0 线程没有被锁
        // writerShouldBlock():判断是否需要阻塞,非公平锁不需要直接CAS
        // 公平锁需要判断阻塞队列中是否存在排队的线程,存在排队直接阻塞,不会CAS
        if (writerShouldBlock() ||
            !compareAndSetState(c, c + acquires))
            return false;
        // 把当前线程设为独占线程
        setExclusiveOwnerThread(current);
        return true;
    }
}

```
2、没有获得锁，通过addWaiter把当前线程封装成Node结点放入AQS队列尾部

3、acquireQueued Node通过自旋取获取锁


## ReadLock  
1、首先要去获取共享锁tryAcquireShared
```java
class ReadLock {
    
    // 获取共享锁
    protected final int tryAcquireShared(int unused) { 
        Thread current = Thread.currentThread();
        int c = getState();
        // 写锁state 二进制后16位 写锁状态不为0 说明存在写线程在做写操作
        // 当前线程和正在写的线程不是一个，阻塞
        if (exclusiveCount(c) != 0 &&
            getExclusiveOwnerThread() != current)
            return -1;
        // 写锁state 二进制前16位 
        int r = sharedCount(c);
        // readerShouldBlock():判断是否需要阻塞,
        // 非公平锁需要判断阻塞队列的第一个结点是不是独占锁(写锁),不是独占锁,CAS
        // 公平锁需要判断阻塞队列中是否存在排队的线程,不存在排队,CAS
        if (!readerShouldBlock() &&
            r < MAX_COUNT &&
            // 在原来基础加2^16 因为前16位标识共享锁状态
            compareAndSetState(c, c + SHARED_UNIT)) {
            if (r == 0) { // 读锁状态为0,没有线程读操作
                firstReader = current; // 设置当前线程为第一个读线程
                firstReaderHoldCount = 1;
            } else if (firstReader == current) {
                // 当前线程 == 第一个读线程 读count自增
                firstReaderHoldCount++;
            } else {
                // 从ThreadLocal共享变量中获取当前线程的HoldCounter,记录次数
                HoldCounter rh = cachedHoldCounter;
                if (rh == null || rh.tid != getThreadId(current))
                    cachedHoldCounter = rh = readHolds.get();
                else if (rh.count == 0)
                    readHolds.set(rh);
                rh.count++;
            }
            return 1;
        }
        // 需要阻塞 或者CAS失败
        return fullTryAcquireShared(current);
    }
    
    // 需要阻塞 或者CAS失败 需要再次尝试获取锁（自旋）
    final int fullTryAcquireShared(Thread current) {
        
        HoldCounter rh = null;
        for (;;) {
            int c = getState();
            
            // 自旋过程中有写锁抢占了线程
            // 写锁状态不为0 说明存在写线程在做写操作
            if (exclusiveCount(c) != 0) {
                // 当前线程和正在写的线程不是一个，阻塞
                if (getExclusiveOwnerThread() != current)
                    return -1; // 获取写锁失败
            } else if (readerShouldBlock()) {
                 
                if (firstReader == current) {
                     
                } else {
                    if (rh == null) {
                        rh = cachedHoldCounter;
                        if (rh == null || rh.tid != getThreadId(current)) {
                            rh = readHolds.get();
                            if (rh.count == 0)
                                readHolds.remove();
                        }
                    }
                    if (rh.count == 0)
                        return -1;
                }
            }
            if (sharedCount(c) == MAX_COUNT)
                throw new Error("Maximum lock count exceeded");
            if (compareAndSetState(c, c + SHARED_UNIT)) {
                if (sharedCount(c) == 0) {
                    firstReader = current;
                    firstReaderHoldCount = 1;
                } else if (firstReader == current) {
                    firstReaderHoldCount++;
                } else {
                    if (rh == null)
                        rh = cachedHoldCounter;
                    if (rh == null || rh.tid != getThreadId(current))
                        rh = readHolds.get();
                    else if (rh.count == 0)
                        readHolds.set(rh);
                    rh.count++;
                    cachedHoldCounter = rh; // cache for release
                }
                return 1;
            }
        }
    }
    
    // 创建readHolds ThreadLocal变量,记录每个线程的读取次数
    private transient ThreadLocalHoldCounter readHolds;
    
    static final class HoldCounter {
        int count = 0; 
        final long tid = getThreadId(Thread.currentThread());
    }
    static final class ThreadLocalHoldCounter
        extends ThreadLocal<HoldCounter> {
        public HoldCounter initialValue() {
            return new HoldCounter();
        }
    }
    
}
```
2、doAcquireShared封装一个共享结点Node.SHARED放入阻塞队列,
（相当于所有的读线程在阻塞队列的位置是相同的）读线程共享同一个结点

3、通过自旋取获取锁