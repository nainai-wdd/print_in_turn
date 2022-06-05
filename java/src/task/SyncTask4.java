package task;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class SyncTask4 implements Runnable {
    // 打印的最大值
    private final int max;
    // 所处任务的序号（执行序号）
    private final int pos;
    // 总共的协作任务数量
    private final int maxPos;
    // 当前待执行任务的序号，共享内存来传递信息
    private final AtomicInteger curPos;
    // 代替synchronized的轻量级锁，提供多个监控器，监控器可以和线程n:m绑定
    private final ReentrantLock lock;
    // 多个监控器，根据索引和任务1：1绑定
    private final Condition[] conditions;
    // 倒计时器，用于主线程接收任务结束信号
    private final CountDownLatch latchForEnd;

    public SyncTask4(int pos, int max, AtomicInteger curPos, CountDownLatch latch, ReentrantLock lock, Condition[] conditions, int maxPos) {
        this.pos = pos;
        this.max = max;
        this.curPos = curPos;
        this.lock = lock;
        this.conditions = conditions;
        this.latchForEnd = latch;
        this.maxPos = maxPos;
    }

    /**
     * 将整个循环放到同步代码块里，循环判断.
     * 如果是自己的位置，则打印，并且通过object.wait-notify机制，唤醒下一个待办任务对应的线程，当前线程释放锁进入等待
     * 如果不是自己的位置，则释放锁等待
     */
    @Override
    public void run() {
        lock.lock();
        try {
            int i = pos;
            final int nextPost = (pos + 1) % maxPos;
            while (i <= max) {
                if (curPos.get() == pos) {
//                    System.out.println("T-" + pos + " " + i);
                    System.out.println(i);
                    i += maxPos;
                    curPos.set(nextPost);
                    conditions[nextPost].signal();
                } else {
                    conditions[pos].await();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
            latchForEnd.countDown();
        }
    }

    public static Runnable[] getTaskList(int max, int maxPos, CountDownLatch endLatch) {
        final ReentrantLock reentrantLock = new ReentrantLock();
        final AtomicInteger curPos = new AtomicInteger(0);
        final Condition[] conditions = new Condition[maxPos];
        for (int i = 0; i < maxPos; i++) {
            conditions[i] = reentrantLock.newCondition();
        }
        Runnable[] runnables = new Runnable[maxPos];
        for (int i = 0; i < maxPos; i++) {
            runnables[i] = new SyncTask4(i, max, curPos, endLatch, reentrantLock, conditions, maxPos);
        }
        return runnables;
    }

}